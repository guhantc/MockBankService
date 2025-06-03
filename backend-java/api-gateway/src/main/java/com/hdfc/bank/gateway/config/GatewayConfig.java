package com.hdfc.bank.gateway.config;

import com.hdfc.bank.gateway.filter.AuthenticationFilter;
import com.hdfc.bank.gateway.filter.LoggingFilter;
import com.hdfc.bank.gateway.filter.RateLimitingFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

/**
 * Gateway Configuration for routing requests to microservices
 */
@Configuration
public class GatewayConfig {

    private final AuthenticationFilter authenticationFilter;
    private final LoggingFilter loggingFilter;
    private final RateLimitingFilter rateLimitingFilter;

    public GatewayConfig(AuthenticationFilter authenticationFilter,
                        LoggingFilter loggingFilter,
                        RateLimitingFilter rateLimitingFilter) {
        this.authenticationFilter = authenticationFilter;
        this.loggingFilter = loggingFilter;
        this.rateLimitingFilter = rateLimitingFilter;
    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // User Service Routes
                .route("user-service", r -> r
                        .path("/api/users/**")
                        .and()
                        .method(HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE)
                        .filters(f -> f
                                .filter(loggingFilter)
                                .filter(rateLimitingFilter)
                                .filter(authenticationFilter)
                                .stripPrefix(1))
                        .uri("lb://user-service"))

                // Account Service Routes
                .route("account-service", r -> r
                        .path("/api/accounts/**")
                        .filters(f -> f
                                .filter(loggingFilter)
                                .filter(rateLimitingFilter)
                                .filter(authenticationFilter)
                                .stripPrefix(1))
                        .uri("lb://account-service"))

                // Loan Service Routes
                .route("loan-service", r -> r
                        .path("/api/loans/**")
                        .filters(f -> f
                                .filter(loggingFilter)
                                .filter(rateLimitingFilter)
                                .filter(authenticationFilter)
                                .stripPrefix(1))
                        .uri("lb://loan-service"))

                // Card Service Routes
                .route("card-service", r -> r
                        .path("/api/cards/**")
                        .filters(f -> f
                                .filter(loggingFilter)
                                .filter(rateLimitingFilter)
                                .filter(authenticationFilter)
                                .stripPrefix(1))
                        .uri("lb://card-service"))

                // Investment Service Routes
                .route("investment-service", r -> r
                        .path("/api/investments/**")
                        .filters(f -> f
                                .filter(loggingFilter)
                                .filter(rateLimitingFilter)
                                .filter(authenticationFilter)
                                .stripPrefix(1))
                        .uri("lb://investment-service"))

                // Transaction Service Routes
                .route("transaction-service", r -> r
                        .path("/api/transactions/**")
                        .filters(f -> f
                                .filter(loggingFilter)
                                .filter(rateLimitingFilter)
                                .filter(authenticationFilter)
                                .stripPrefix(1))
                        .uri("lb://transaction-service"))

                // Notification Service Routes
                .route("notification-service", r -> r
                        .path("/api/notifications/**")
                        .filters(f -> f
                                .filter(loggingFilter)
                                .filter(rateLimitingFilter)
                                .filter(authenticationFilter)
                                .stripPrefix(1))
                        .uri("lb://notification-service"))

                // SAGA Orchestrator Routes
                .route("saga-orchestrator", r -> r
                        .path("/api/saga/**")
                        .filters(f -> f
                                .filter(loggingFilter)
                                .filter(rateLimitingFilter)
                                .filter(authenticationFilter)
                                .stripPrefix(1))
                        .uri("lb://saga-orchestrator"))

                // Public routes (no authentication needed)
                .route("public-login", r -> r
                        .path("/api/auth/login", "/api/auth/register")
                        .filters(f -> f
                                .filter(loggingFilter)
                                .filter(rateLimitingFilter)
                                .stripPrefix(1))
                        .uri("lb://user-service"))

                .build();
    }
}