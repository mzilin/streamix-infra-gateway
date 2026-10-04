package com.mariuszilinskas.streamix.infra.gateway.filter;

import com.mariuszilinskas.streamix.infra.gateway.properties.AppProperties;
import com.mariuszilinskas.streamix.infra.gateway.dto.JwtPayload;
import com.mariuszilinskas.streamix.infra.gateway.service.JwtService;
import com.mariuszilinskas.streamix.infra.gateway.util.AppUtils;
import com.mariuszilinskas.streamix.infra.gateway.util.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private WebFilterChain chain;

    private AuthenticationFilter filter;

    private static final String userId = TestUtils.userId.toString();

    private static final JwtPayload payload = new JwtPayload(
            userId, List.of("USER", "ADMIN"), List.of("MANAGE_SETTINGS"), new Date()
    );

    @BeforeEach
    void setUp() {
        AppProperties.SecurityPaths security = new AppProperties.SecurityPaths(
                List.of("/", "/actuator/health"),
                List.of("/actuator/refresh", "/api/v1/auth/login", "/api/v1/session/logout/**"),
                List.of(),
                List.of("/api/v1/account/admin/**")
        );
        AppProperties appProps = new AppProperties(List.of(), security);
        filter = new AuthenticationFilter(jwtService, appProps);
    }

    // ------------------------------------

    @Test
    void testFilter_PublicPath_BypassesAuthentication() {
        // Arrange
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/actuator/health").build()
        );
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        // Act
        filter.filter(exchange, chain).block();

        // Assert
        verify(jwtService, never()).extractPayload(any());
        verify(chain).filter(exchange);
    }

    // ------------------------------------

    @Test
    void testFilter_PrivatePath_MissingToken_ReturnsUnauthorized() {
        // Arrange
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/users/profile").build()
        );
        when(jwtService.extractPayload(exchange)).thenReturn(Optional.empty());

        // Act
        filter.filter(exchange, chain).block();

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        verify(chain, never()).filter(any());
    }

    // ------------------------------------

    @Test
    void testFilter_ValidToken_SetsUserIdHeaderOnRequest() {
        // Arrange
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/users/profile").build()
        );
        when(jwtService.extractPayload(exchange)).thenReturn(Optional.of(payload));

        ArgumentCaptor<ServerWebExchange> captor = ArgumentCaptor.forClass(ServerWebExchange.class);
        when(chain.filter(captor.capture())).thenReturn(Mono.empty());

        // Act
        filter.filter(exchange, chain).block();

        // Assert
        assertEquals(userId, captor.getValue().getRequest().getHeaders().getFirst(AppUtils.USER_ID_HEADER));
    }

    @Test
    void testFilter_ValidToken_NullRolesAndAuthorities_CallsChain() {
        // Arrange
        JwtPayload nullPayload = new JwtPayload(userId, null, null, new Date());
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/users/profile").build()
        );
        when(jwtService.extractPayload(exchange)).thenReturn(Optional.of(nullPayload));
        when(chain.filter(any())).thenReturn(Mono.empty());

        // Act & Assert
        assertDoesNotThrow(() -> filter.filter(exchange, chain).block());
        verify(chain).filter(any());
    }

}
