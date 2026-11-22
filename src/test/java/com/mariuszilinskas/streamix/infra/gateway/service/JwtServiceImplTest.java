package com.mariuszilinskas.streamix.infra.gateway.service;

import com.mariuszilinskas.streamix.infra.gateway.dto.JwtPayload;
import com.mariuszilinskas.streamix.infra.gateway.enums.UserAuthority;
import com.mariuszilinskas.streamix.infra.gateway.enums.UserRole;
import com.mariuszilinskas.streamix.infra.gateway.exception.JwtTokenValidationException;
import com.mariuszilinskas.streamix.infra.gateway.properties.SecurityProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.mariuszilinskas.streamix.infra.gateway.TestConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JwtServiceImplTest {

    @Mock
    private ServerWebExchange exchange;

    @Mock
    private ServerHttpRequest request;

    @Mock
    private HttpCookie httpCookie;

    private JwtServiceImpl jwtService;

    private static MultiValueMap<String, HttpCookie> cookies;

    // ------------------------------------

    @BeforeEach
    void setup() {
        SecurityProperties securityProperties = new SecurityProperties(TEST_SECRET_KEY, Map.of());
        jwtService = new JwtServiceImpl(securityProperties);

        cookies = new LinkedMultiValueMap<>();
        cookies.add(TEST_ACCESS_TOKEN_NAME, httpCookie);
    }

    // ------------------------------------

    @Test
    void testExtractPayload_ValidToken() {
        // Arrange
        when(exchange.getRequest()).thenReturn(request);
        when(request.getCookies()).thenReturn(cookies);
        when(httpCookie.getValue()).thenReturn(TEST_ACCESS_TOKEN);

        // Act
        Optional<JwtPayload> response = jwtService.extractPayload(exchange);

        // Assert
        assertTrue(response.isPresent());
        JwtPayload payload = response.get();
        assertEquals(TEST_USER_ID, payload.userId());
        assertEquals(List.of(UserRole.USER.name(), UserRole.ADMIN.name()), payload.roles());
        assertEquals(List.of(UserAuthority.MANAGE_SETTINGS.name()), payload.authorities());
        assertEquals(new Date(3295380670000L), payload.expiry());
    }

    @Test
    void testExtractPayload_InvalidToken() {
        // Arrange
        when(exchange.getRequest()).thenReturn(request);
        when(request.getCookies()).thenReturn(cookies);
        when(httpCookie.getValue()).thenReturn("invalidAccessToken");

        // Act & Assert
        assertThrows(JwtTokenValidationException.class, () -> jwtService.extractPayload(exchange));
    }

    // ------------------------------------

    @Test
    void testSafelyExtractListFromClaims() {
        // Arrange
        Map<String, Object> claims = Map.of(
                "roles", List.of(UserRole.USER.name(), UserRole.ADMIN.name()),
                "authorities", List.of(UserAuthority.MANAGE_SETTINGS.name())
        );

        // Act
        List<String> roles = jwtService.safelyExtractListFromClaims(claims, "roles");
        List<String> authorities = jwtService.safelyExtractListFromClaims(claims, "authorities");

        // Assert
        assertEquals(List.of(UserRole.USER.name(), UserRole.ADMIN.name()), roles);
        assertEquals(List.of(UserAuthority.MANAGE_SETTINGS.name()), authorities);
    }

    // ------------------------------------

    @Test
    void testExtractAccessToken_ValidToken() {
        // Arrange
        when(exchange.getRequest()).thenReturn(request);
        when(request.getCookies()).thenReturn(cookies);
        when(httpCookie.getValue()).thenReturn(TEST_ACCESS_TOKEN);

        // Act
        String response = jwtService.extractAccessToken(exchange);

        // Assert
        assertNotNull(response);
        assertEquals(TEST_ACCESS_TOKEN, response);
    }

    @Test
    void testExtractAccessToken_MissingCookie_ReturnsNull() {
        // Arrange
        when(exchange.getRequest()).thenReturn(request);
        when(request.getCookies()).thenReturn(new LinkedMultiValueMap<>());

        // Act
        String response = jwtService.extractAccessToken(exchange);

        // Assert
        assertNull(response);
    }

}
