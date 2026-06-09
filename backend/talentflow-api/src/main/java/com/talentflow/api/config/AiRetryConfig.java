package com.talentflow.api.config;

import com.talentflow.api.exception.AiUnavailableException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class AiRetryConfig {

    /**
     * Retries transient Gemini failures only. Quota errors fail fast (no retry)
     * because HTTP 429 will not recover within seconds.
     */
    @Bean
    public RetryTemplate geminiRetryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();

        Map<Class<? extends Throwable>, Boolean> retryable = new HashMap<>();
        retryable.put(AiUnavailableException.class, true);
        retryTemplate.setRetryPolicy(new SimpleRetryPolicy(3, retryable, true));

        ExponentialBackOffPolicy backOff = new ExponentialBackOffPolicy();
        backOff.setInitialInterval(2000L);
        backOff.setMultiplier(2.5);
        backOff.setMaxInterval(10000L);
        retryTemplate.setBackOffPolicy(backOff);

        return retryTemplate;
    }
}
