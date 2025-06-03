package com.hdfc.bank.eureka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * HDFC Bank Eureka Server - Service Discovery
 * 
 * This service acts as a registry for all microservices in the HDFC Bank ecosystem.
 * All microservices register themselves with this server for service discovery.
 * 
 * @author HDFC Bank Development Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableEurekaServer
public class EurekaServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }
}