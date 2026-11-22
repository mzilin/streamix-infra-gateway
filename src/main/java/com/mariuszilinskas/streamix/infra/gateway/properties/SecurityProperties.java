package com.mariuszilinskas.streamix.infra.gateway.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

/**
 * Binds security configuration, including the JWT access token secret and per-cluster API keys.
 */
@ConfigurationProperties(prefix = "security")
public record SecurityProperties(
        String accessTokenSecret,
        Map<String, String> clusterKeys
) {}
