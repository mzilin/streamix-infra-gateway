package com.mariuszilinskas.streamix.infra.gateway.config;

import com.mariuszilinskas.streamix.infra.gateway.enums.UserRole;
import com.mariuszilinskas.streamix.infra.gateway.filter.AuthenticationFilter;
import com.mariuszilinskas.streamix.infra.gateway.filter.UserIdFilter;
import com.mariuszilinskas.streamix.infra.gateway.properties.AppProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.reactive.CorsConfigurationSource;

import java.util.List;

@Configuration
@EnableMethodSecurity
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CorsConfigurationSource corsConfigurationSource;
    private final AuthenticationFilter authenticationFilter;
    private final UserIdFilter userIdFilter;
    private final AppProperties appProperties;

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        return http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(this::configureAuthorization)
                .addFilterAt(authenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .addFilterAfter(userIdFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }

    private void configureAuthorization(ServerHttpSecurity.AuthorizeExchangeSpec authorization) {
        var security = appProperties.security();

        authorization
                .pathMatchers(HttpMethod.GET, toArray(security.publicGetPaths())).permitAll()
                .pathMatchers(HttpMethod.POST, toArray(security.publicPostPaths())).permitAll();

        if (!security.anyMethodPaths().isEmpty()) {
            authorization.pathMatchers(toArray(security.anyMethodPaths())).permitAll();
        }

        authorization
                .pathMatchers(toArray(security.adminPaths()))
                .hasRole(UserRole.ADMIN.name())
                .anyExchange()
                .authenticated();
    }

    private String[] toArray(List<String> list) {
        return list.toArray(String[]::new);
    }

}
