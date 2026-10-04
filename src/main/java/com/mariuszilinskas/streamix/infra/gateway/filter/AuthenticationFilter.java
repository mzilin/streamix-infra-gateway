package com.mariuszilinskas.streamix.infra.gateway.filter;

import com.mariuszilinskas.streamix.infra.gateway.properties.AppProperties;
import com.mariuszilinskas.streamix.infra.gateway.dto.JwtPayload;
import com.mariuszilinskas.streamix.infra.gateway.service.JwtService;
import com.mariuszilinskas.streamix.infra.gateway.util.AppUtils;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Stream;

@NullMarked
@Component
@RequiredArgsConstructor
public class AuthenticationFilter implements WebFilter {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationFilter.class);

    private final JwtService jwtService;
    private final AppProperties appProps;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        if (isPublicPath(exchange.getRequest())) {
            return chain.filter(exchange);
        }

        var payloadOpt = jwtService.extractPayload(exchange);
        if (payloadOpt.isEmpty()) {
            logger.error("Access token is missing for a protected path");
            return onError(exchange);
        }

        JwtPayload payload = payloadOpt.get();
        String userId = payload.userId();
        List<GrantedAuthority> authorities = extractAuthorities(payload);

        var authentication = new UsernamePasswordAuthenticationToken(userId, null, authorities);
        logger.info("Set security context {}", payload);

        ServerWebExchange mutatedExchange = exchange.mutate()
                .request(r -> r.headers(h -> h.set(AppUtils.USER_ID_HEADER, userId)))
                .build();

        return chain.filter(mutatedExchange)
                .contextWrite(ctx -> {
                    Map<String, String> merged = new HashMap<>(
                            Objects.requireNonNull(ctx.getOrDefault(AppUtils.MDC_CONTEXT_KEY, Map.of()))
                    );
                    merged.put(AppUtils.MDC_USER_ID, userId);
                    return ctx.put(AppUtils.MDC_CONTEXT_KEY, merged);
                })
                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));
    }

    private boolean isPublicPath(ServerHttpRequest request) {
        String path = request.getURI().getPath();
        var security = appProps.security();
        return Stream.of(
                        security.publicGetPaths(),
                        security.publicPostPaths(),
                        security.anyMethodPaths()
                ).flatMap(List::stream)
                .map(p -> p.endsWith("/**") ? p.substring(0, p.length() - 3) : p)
                .anyMatch(p -> path.equals(p) || path.startsWith(p + "/"));
    }

    private List<GrantedAuthority> extractAuthorities(JwtPayload payload) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        if (payload.roles() != null) {
            authorities.addAll(payload.roles().stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                    .toList());
        }
        if (payload.authorities() != null) {
            authorities.addAll(payload.authorities().stream()
                    .map(SimpleGrantedAuthority::new)
                    .toList());
        }
        return authorities;
    }

    private Mono<Void> onError(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        return response.setComplete();
    }

}
