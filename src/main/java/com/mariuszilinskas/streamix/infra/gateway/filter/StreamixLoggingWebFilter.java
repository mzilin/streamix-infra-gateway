package com.mariuszilinskas.streamix.infra.gateway.filter;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.core.Ordered;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.UUID;

import static com.mariuszilinskas.streamix.observability.context.LogContext.*;

/**
 * Assigns a correlation ID to every request and seeds the reactive MDC context with service
 * and environment metadata.
 */
public final class StreamixLoggingWebFilter implements WebFilter, Ordered {

    private final String serviceName;
    private final String environment;

    public StreamixLoggingWebFilter(String serviceName, String environment) {
        this.serviceName = serviceName;
        this.environment = environment;
    }

    @Override
    @NonNull
    public Mono<Void> filter(
            @NonNull ServerWebExchange exchange,
            @NonNull WebFilterChain chain
    ) {
        String correlationId = resolveCorrelationId(
                exchange.getRequest().getHeaders().getFirst(CORRELATION_ID_HEADER)
        );

        exchange.getResponse().getHeaders().set(CORRELATION_ID_HEADER, correlationId);

        ServerWebExchange mutatedExchange = exchange.mutate()
                .request(r -> r.headers(h -> {
                    h.set(CORRELATION_ID_HEADER, correlationId);
                    h.remove(USER_ID_HEADER);
                }))
                .build();

        Map<String, String> mdcMap = Map.of(
                MDC_CORRELATION_ID, correlationId,
                MDC_SERVICE, serviceName,
                MDC_ENVIRONMENT, environment
        );

        return chain
                .filter(mutatedExchange)
                .contextWrite(ctx -> ctx.put(MDC_CONTEXT_KEY, mdcMap));
    }

    private static String resolveCorrelationId(@Nullable String value) {
        return isValidCorrelationId(value) ? value : UUID.randomUUID().toString();
    }

    private static boolean isValidCorrelationId(@Nullable String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        try {
            UUID.fromString(value);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

}
