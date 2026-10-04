package com.mariuszilinskas.streamix.infra.gateway.service;

import com.mariuszilinskas.streamix.infra.gateway.properties.SecurityProperties;
import com.mariuszilinskas.streamix.infra.gateway.exception.JwtTokenValidationException;
import com.mariuszilinskas.streamix.infra.gateway.dto.JwtPayload;
import com.mariuszilinskas.streamix.infra.gateway.util.AppUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpCookie;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;

import javax.crypto.SecretKey;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service implementation for managing JWT tokens.
 *
 * @author Marius Zilinskas
 */
@Service
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {

    private final SecurityProperties securityProps;

    @Override
    public Optional<JwtPayload> extractPayload(ServerWebExchange exchange) {
        String token = extractAccessToken(exchange);

        if (token != null && !token.isBlank()) {
            Claims claims = extractClaims(token);

            JwtPayload payload = new JwtPayload(
                    claims.getSubject(),
                    safelyExtractListFromClaims(claims, "roles"),
                    safelyExtractListFromClaims(claims, "authorities"),
                    claims.getExpiration()
            );

            return Optional.of(payload);
        }

        return Optional.empty();
    }

    @Override
    public String extractAccessToken(ServerWebExchange exchange) {
        return exchange.getRequest()
                .getCookies()
                .getOrDefault(AppUtils.ACCESS_TOKEN_NAME, Collections.emptyList())
                .stream()
                .findFirst()
                .map(HttpCookie::getValue)
                .orElse(null);
    }

    protected List<String> safelyExtractListFromClaims(Map<String, Object> claims, String claimKey) {
        Object claimValue = claims.get(claimKey);
        if (claimValue instanceof List<?> rawList) {
            return rawList.stream()
                    .filter(obj -> obj instanceof String)
                    .map(obj -> (String) obj)
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    private Claims extractClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getAccessTokenSecret())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (JwtException ex) {
            throw new JwtTokenValidationException();
        }
    }

    private SecretKey getAccessTokenSecret() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(securityProps.accessTokenSecret()));
    }

}
