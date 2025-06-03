package com.hdfc.bank.gateway.filter;

import com.hdfc.bank.gateway.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthenticationFilter
 */
@ExtendWith(MockitoExtension.class)
class AuthenticationFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private GatewayFilterChain filterChain;

    @InjectMocks
    private AuthenticationFilter authenticationFilter;

    private ServerWebExchange exchange;
    private ServerHttpRequest request;
    private ServerHttpResponse response;

    @BeforeEach
    void setUp() {
        request = MockServerHttpRequest.get("/api/users/123").build();
        exchange = MockServerWebExchange.from(request);
        response = exchange.getResponse();
    }

    @Test
    void filter_ValidToken_Success() {
        // Given
        String validToken = "valid.jwt.token";
        request = MockServerHttpRequest.get("/api/users/123")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken)
                .build();
        exchange = MockServerWebExchange.from(request);

        when(jwtUtil.validateToken(validToken)).thenReturn(true);
        when(jwtUtil.getUsernameFromToken(validToken)).thenReturn("john.doe@example.com");
        when(jwtUtil.getUserIdFromToken(validToken)).thenReturn("USER001");
        when(filterChain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        // When
        Mono<Void> result = authenticationFilter.filter(exchange, filterChain);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        verify(jwtUtil).validateToken(validToken);
        verify(jwtUtil).getUsernameFromToken(validToken);
        verify(jwtUtil).getUserIdFromToken(validToken);
        verify(filterChain).filter(any(ServerWebExchange.class));
    }

    @Test
    void filter_NoAuthorizationHeader_Unauthorized() {
        // Given
        request = MockServerHttpRequest.get("/api/users/123").build();
        exchange = MockServerWebExchange.from(request);

        // When
        Mono<Void> result = authenticationFilter.filter(exchange, filterChain);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        verify(jwtUtil, never()).validateToken(anyString());
        verify(filterChain, never()).filter(any(ServerWebExchange.class));
    }

    @Test
    void filter_InvalidAuthorizationHeader_Unauthorized() {
        // Given
        request = MockServerHttpRequest.get("/api/users/123")
                .header(HttpHeaders.AUTHORIZATION, "Invalid header")
                .build();
        exchange = MockServerWebExchange.from(request);

        // When
        Mono<Void> result = authenticationFilter.filter(exchange, filterChain);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        verify(jwtUtil, never()).validateToken(anyString());
        verify(filterChain, never()).filter(any(ServerWebExchange.class));
    }

    @Test
    void filter_InvalidToken_Unauthorized() {
        // Given
        String invalidToken = "invalid.jwt.token";
        request = MockServerHttpRequest.get("/api/users/123")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + invalidToken)
                .build();
        exchange = MockServerWebExchange.from(request);

        when(jwtUtil.validateToken(invalidToken)).thenReturn(false);

        // When
        Mono<Void> result = authenticationFilter.filter(exchange, filterChain);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        verify(jwtUtil).validateToken(invalidToken);
        verify(filterChain, never()).filter(any(ServerWebExchange.class));
    }

    @Test
    void filter_PublicEndpoint_SkipsAuthentication() {
        // Given
        request = MockServerHttpRequest.get("/api/auth/login").build();
        exchange = MockServerWebExchange.from(request);

        when(filterChain.filter(exchange)).thenReturn(Mono.empty());

        // When
        Mono<Void> result = authenticationFilter.filter(exchange, filterChain);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        verify(jwtUtil, never()).validateToken(anyString());
        verify(filterChain).filter(exchange);
    }

    @Test
    void filter_HealthEndpoint_SkipsAuthentication() {
        // Given
        request = MockServerHttpRequest.get("/actuator/health").build();
        exchange = MockServerWebExchange.from(request);

        when(filterChain.filter(exchange)).thenReturn(Mono.empty());

        // When
        Mono<Void> result = authenticationFilter.filter(exchange, filterChain);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        verify(jwtUtil, never()).validateToken(anyString());
        verify(filterChain).filter(exchange);
    }

    @Test
    void filter_TokenValidationException_Unauthorized() {
        // Given
        String tokenWithException = "exception.jwt.token";
        request = MockServerHttpRequest.get("/api/users/123")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenWithException)
                .build();
        exchange = MockServerWebExchange.from(request);

        when(jwtUtil.validateToken(tokenWithException)).thenThrow(new RuntimeException("Token parsing error"));

        // When
        Mono<Void> result = authenticationFilter.filter(exchange, filterChain);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        verify(jwtUtil).validateToken(tokenWithException);
        verify(filterChain, never()).filter(any(ServerWebExchange.class));
    }

    private void assertEquals(HttpStatus expected, HttpStatus actual) {
        assert expected.equals(actual);
    }
}