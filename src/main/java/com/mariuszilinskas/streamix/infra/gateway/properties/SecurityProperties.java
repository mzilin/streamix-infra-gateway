package com.mariuszilinskas.streamix.infra.gateway.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@ConfigurationProperties(prefix = "security")
public record SecurityProperties(
        String accessTokenSecret,
        Map<String, String> clusterKeys
) {}
