package com.hdfc.bank.saga;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * HDFC Bank SAGA Orchestrator Application
 * 
 * This service implements the SAGA pattern for managing distributed transactions
 * across multiple microservices. It ensures data consistency and handles
 * compensation activities in case of failures.
 * 
 * Key SAGAs implemented:
 * 1. Account Opening SAGA - User creation + Account setup + Card issuance
 * 2. Loan Processing SAGA - Application + Approval + Disbursement
 * 3. Fund Transfer SAGA - Debit + Credit + Notification
 * 
 * @author HDFC Bank Development Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableAsync
public class SagaOrchestratorApplication {

    public static void main(String[] args) {
        SpringApplication.run(SagaOrchestratorApplication.class, args);
    }
}