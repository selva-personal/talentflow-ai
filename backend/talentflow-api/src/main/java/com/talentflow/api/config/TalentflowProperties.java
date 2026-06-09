package com.talentflow.api.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "talentflow")
public class TalentflowProperties {

    private Jwt jwt = new Jwt();
    private Cors cors = new Cors();
    private Gemini gemini = new Gemini();
    private OpenAi openai = new OpenAi();
    private Anthropic anthropic = new Anthropic();
    private Ollama ollama = new Ollama();
    private Ai ai = new Ai();
    private Upload upload = new Upload();
    private RateLimit rateLimit = new RateLimit();

    @Getter
    @Setter
    public static class Jwt {
        private String secret;
        private long accessExpirationMs;
        private long refreshExpirationMs;
    }

    @Getter
    @Setter
    public static class Cors {
        private String allowedOrigins;
    }

    @Getter
    @Setter
    public static class Gemini {
        private String apiKey;
        private String model;
    }

    @Getter
    @Setter
    public static class OpenAi {
        private String apiKey;
        private String model;
        private String baseUrl;
    }

    @Getter
    @Setter
    public static class Anthropic {
        private String apiKey;
        private String model;
        private String baseUrl;
    }

    @Getter
    @Setter
    public static class Ollama {
        private String baseUrl;
        private String model;
    }

    @Getter
    @Setter
    public static class Ai {
        /** gemini | fallback — APP_AI_MODE */
        private String mode = "gemini";
        /** gemini | openai | anthropic | ollama | fallback — AI_PROVIDER */
        private String provider = "gemini";
    }

    @Getter
    @Setter
    public static class Upload {
        private String directory;
    }

    @Getter
    @Setter
    public static class RateLimit {
        private int requestsPerMinute;
    }
}
