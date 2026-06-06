package com.talentflow.api.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.talentflow.api.dto.response.ApiResponse;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final TalentflowProperties properties;
    private final ObjectMapper objectMapper;
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientKey = resolveClientKey(request);
        Bucket bucket = buckets.computeIfAbsent(clientKey, key -> createBucket());
        if (!bucket.tryConsume(1)) {
            response.setStatus(429);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            objectMapper.writeValue(response.getWriter(),
                    ApiResponse.error("Too many requests. Please try again later."));
            return;
        }
        filterChain.doFilter(request, response);
    }

    private Bucket createBucket() {
        int limit = Math.max(properties.getRateLimit().getRequestsPerMinute(), 30);
        Bandwidth bandwidth = Bandwidth.classic(limit, Refill.greedy(limit, Duration.ofMinutes(1)));
        return Bucket.builder().addLimit(bandwidth).build();
    }

    private String resolveClientKey(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
