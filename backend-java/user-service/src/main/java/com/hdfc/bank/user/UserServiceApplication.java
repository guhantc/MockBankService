package com.hdfc.bank.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * HDFC Bank User Service Application
 * 
 * This service handles:
 * - User registration and authentication
 * - User profile management
 * - JWT token generation and validation
 * - User roles and permissions
 * - Password management and security
 * 
 * @author HDFC Bank Development Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableDiscoveryClient
public class UserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}