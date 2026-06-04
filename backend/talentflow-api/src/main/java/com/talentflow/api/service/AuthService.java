package com.talentflow.api.service;

import com.talentflow.api.config.TalentflowProperties;
import com.talentflow.api.dto.request.*;
import com.talentflow.api.dto.response.AuthResponse;
import com.talentflow.api.dto.response.UserResponse;
import com.talentflow.api.entity.RefreshToken;
import com.talentflow.api.entity.Role;
import com.talentflow.api.entity.User;
import com.talentflow.api.exception.BadRequestException;
import com.talentflow.api.exception.ResourceNotFoundException;
import com.talentflow.api.repository.RefreshTokenRepository;
import com.talentflow.api.repository.RoleRepository;
import com.talentflow.api.repository.UserRepository;
import com.talentflow.api.security.JwtService;
import com.talentflow.api.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final TalentflowProperties properties;
    private final ActivityLogService activityLogService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already registered");
        }
        Role role = roleRepository.findByName("USER")
                .orElseThrow(() -> new ResourceNotFoundException("Default role not found"));
        User user = User.builder()
                .email(request.getEmail().toLowerCase())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .role(role)
                .enabled(true)
                .emailVerified(false)
                .build();
        user = userRepository.save(user);
        activityLogService.log(user.getId(), "USER_REGISTERED", "USER", user.getId(), null);
        return buildAuthResponse(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail().toLowerCase(), request.getPassword()));
        User user = userRepository.findByEmail(request.getEmail().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setLastLoginAt(Instant.now());
        userRepository.save(user);
        activityLogService.log(user.getId(), "USER_LOGIN", "USER", user.getId(), null);
        return buildAuthResponse(user);
    }

    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request) {
        String hash = hashToken(request.getRefreshToken());
        RefreshToken token = refreshTokenRepository.findByTokenHashAndRevokedFalse(hash)
                .orElseThrow(() -> new BadRequestException("Invalid refresh token"));
        if (token.getExpiresAt().isBefore(Instant.now())) {
            throw new BadRequestException("Refresh token expired");
        }
        User user = token.getUser();
        return buildAuthResponse(user);
    }

    @Transactional
    public void logout(Long userId) {
        refreshTokenRepository.revokeAllByUserId(userId);
    }

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        userRepository.findByEmail(request.getEmail().toLowerCase()).ifPresent(user -> {
            String token = UUID.randomUUID().toString();
            user.setResetToken(token);
            user.setResetTokenExp(Instant.now().plusSeconds(3600));
            userRepository.save(user);
        });
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByResetToken(request.getToken())
                .orElseThrow(() -> new BadRequestException("Invalid reset token"));
        if (user.getResetTokenExp() == null || user.getResetTokenExp().isBefore(Instant.now())) {
            throw new BadRequestException("Reset token expired");
        }
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setResetToken(null);
        user.setResetTokenExp(null);
        userRepository.save(user);
        refreshTokenRepository.revokeAllByUserId(user.getId());
    }

    private AuthResponse buildAuthResponse(User user) {
        UserPrincipal principal = new UserPrincipal(user);
        String accessToken = jwtService.generateAccessToken(principal);
        String refreshValue = jwtService.generateRefreshTokenValue();
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .tokenHash(hashToken(refreshValue))
                .expiresAt(Instant.now().plusMillis(properties.getJwt().getRefreshExpirationMs()))
                .revoked(false)
                .build();
        refreshTokenRepository.save(refreshToken);
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshValue)
                .user(UserResponse.from(user))
                .build();
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new IllegalStateException("Hash failed", e);
        }
    }
}
