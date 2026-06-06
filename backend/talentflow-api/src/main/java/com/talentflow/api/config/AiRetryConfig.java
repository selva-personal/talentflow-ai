package com.talentflow.api.config;

import com.talentflow.api.exception.AiQuotaExceededException;
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

    @Bean
    public RetryTemplate geminiRetryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();

        Map<Class<? extends Throwable>, Boolean> retryable = new HashMap<>();
        retryable.put(AiQuotaExceededException.class, true);
        retryable.put(AiUnavailableException.class, true);
        retryTemplate.setRetryPolicy(new SimpleRetryPolicy(4, retryable, true));

        ExponentialBackOffPolicy backOff = new ExponentialBackOffPolicy();
        backOff.setInitialInterval(2000L);
        backOff.setMultiplier(2.5);
        backOff.setMaxInterval(10000L);
        retryTemplate.setBackOffPolicy(backOff);

        return retryTemplate;
    }
}
