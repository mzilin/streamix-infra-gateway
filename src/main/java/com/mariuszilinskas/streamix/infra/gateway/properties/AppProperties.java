package com.mariuszilinskas.streamix.infra.gateway.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Binds application-level configuration, route definitions and security path classifications.
 */
@ConfigurationProperties(prefix = "app")
public record AppProperties(
        List<RouteDefinition> routes,
        SecurityPaths security
) {

    public record RouteDefinition(String path, String cluster, String service) {}

    public record SecurityPaths(
            List<String> publicGetPaths,
            List<String> publicPostPaths,
            List<String> anyMethodPaths,
            List<String> adminPaths
    ) {}

}
