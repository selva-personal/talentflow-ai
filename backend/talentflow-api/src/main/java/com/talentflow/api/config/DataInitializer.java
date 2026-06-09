package com.talentflow.api.config;

import com.talentflow.api.ai.ApiKeyUtils;
import com.talentflow.api.entity.Role;
import com.talentflow.api.entity.User;
import com.talentflow.api.exception.ResourceNotFoundException;
import com.talentflow.api.repository.RoleRepository;
import com.talentflow.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    public static final String ADMIN_EMAIL = "admin@talentflow.ai";
    public static final String ADMIN_PASSWORD = "Admin@123";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${talentflow.gemini.api-key:}")
    private String geminiApiKey;

    @Override
    public void run(ApplicationArguments args) {
        if (geminiApiKey == null || geminiApiKey.isBlank()) {
            log.warn("GEMINI_API_KEY is not set — AI features will fail. Add it to backend/talentflow-api/.env or use ./scripts/run-backend.sh");
        } else {
            log.info("Gemini API key loaded: masked={} format={} length={}",
                    ApiKeyUtils.mask(geminiApiKey),
                    ApiKeyUtils.describeFormat(geminiApiKey),
                    geminiApiKey.trim().length());
        }

        if (userRepository.findByEmail(ADMIN_EMAIL).isPresent()) {
            return;
        }
        Role adminRole = roleRepository.findByName("ADMIN")
                .orElseThrow(() -> new ResourceNotFoundException("ADMIN role not found"));
        User admin = User.builder()
                .email(ADMIN_EMAIL)
                .passwordHash(passwordEncoder.encode(ADMIN_PASSWORD))
                .firstName("System")
                .lastName("Admin")
                .role(adminRole)
                .enabled(true)
                .emailVerified(true)
                .build();
        userRepository.save(admin);
        log.info("Default admin account created: {}", ADMIN_EMAIL);
    }
}
