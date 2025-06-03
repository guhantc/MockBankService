package com.hdfc.bank.eureka;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Unit tests for Eureka Server Application
 */
@SpringBootTest
@ActiveProfiles("test")
class EurekaServerApplicationTest {

    @Test
    void contextLoads() {
        // Test that the application context loads successfully
    }

    @Test
    void testEurekaServerStartup() {
        // Test that Eureka server starts up correctly
        // This test verifies that all necessary beans are created
        // and the application can start without errors
    }
}