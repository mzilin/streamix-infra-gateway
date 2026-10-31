package com.mariuszilinskas.streamix.infra.gateway.config;

import com.mariuszilinskas.streamix.infra.gateway.filter.StreamixLoggingWebFilter;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.web.server.WebFilter;
import reactor.core.publisher.Hooks;

@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
@ConditionalOnClass(WebFilter.class)
public class ReactiveObservabilityAutoConfiguration {

    @PostConstruct
    public void enableContextPropagation() {
        Hooks.enableAutomaticContextPropagation();
    }

    @Bean
    @ConditionalOnMissingBean(StreamixLoggingWebFilter.class)
    public StreamixLoggingWebFilter streamixLoggingWebFilter(Environment env) {
        String serviceName = env.getProperty("spring.application.name", "");
        String environment = String.join(",", env.getActiveProfiles());
        return new StreamixLoggingWebFilter(serviceName, environment);
    }
}
