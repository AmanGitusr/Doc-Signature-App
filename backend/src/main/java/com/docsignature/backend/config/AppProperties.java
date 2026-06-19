package com.docsignature.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppProperties(
        String baseUrl,
        String storageDir,
        String corsAllowedOrigins,
        Jwt jwt
) {
    public record Jwt(String secret, long expirationMinutes) {}
}
