package com.hdfc.bank.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * HDFC Bank API Gateway Application
 * 
 * This service acts as the single entry point for all client requests
 * to the HDFC Bank microservices ecosystem. It provides:
 * - Request routing to appropriate microservices
 * - Authentication and authorization
 * - Rate limiting and throttling
 * - Request/response transformation
 * - Circuit breaker patterns
 * 
 * @author HDFC Bank Development Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableDiscoveryClient
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}