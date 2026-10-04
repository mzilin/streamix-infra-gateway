package com.mariuszilinskas.streamix.infra.gateway.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "frontend")
public record FrontendProperties(
        String baseUrl,
        String allowedOrigins
) {}
