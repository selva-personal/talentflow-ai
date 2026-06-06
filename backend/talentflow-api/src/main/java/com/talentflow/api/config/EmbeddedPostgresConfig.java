package com.talentflow.api.config;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;
import java.io.IOException;

/**
 * Local dev profile when Docker is unavailable or port 5432 uses different credentials.
 * Starts PostgreSQL on port 5433 with Flyway-compatible Postgres.
 */
@Slf4j
@Configuration
@Profile("dev")
public class EmbeddedPostgresConfig {

    private static final int EMBEDDED_PORT = 5433;

    @Bean(destroyMethod = "close")
    public EmbeddedPostgres embeddedPostgres() throws IOException {
        log.info("Starting embedded PostgreSQL for dev profile on port {}", EMBEDDED_PORT);
        return EmbeddedPostgres.builder()
                .setPort(EMBEDDED_PORT)
                .start();
    }

    @Bean
    @Primary
    public DataSource dataSource(EmbeddedPostgres embeddedPostgres) {
        return embeddedPostgres.getPostgresDatabase();
    }
}
