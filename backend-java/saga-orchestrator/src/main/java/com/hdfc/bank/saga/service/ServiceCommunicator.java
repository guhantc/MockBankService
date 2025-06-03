package com.hdfc.bank.saga.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * Service Communicator for inter-service communication
 */
@Service
public class ServiceCommunicator {

    private static final Logger logger = LoggerFactory.getLogger(ServiceCommunicator.class);

    private final WebClient webClient;

    public ServiceCommunicator(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    public String createUser(String userPayload) {
        try {
            return webClient.post()
                    .uri("http://user-service/users")
                    .bodyValue(userPayload)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (Exception e) {
            logger.error("Failed to create user", e);
            throw new RuntimeException("User creation failed", e);
        }
    }

    public String createAccount(String userId) {
        try {
            return webClient.post()
                    .uri("http://account-service/accounts")
                    .bodyValue("{\"userId\":\"" + userId + "\"}")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (Exception e) {
            logger.error("Failed to create account for user: {}", userId, e);
            throw new RuntimeException("Account creation failed", e);
        }
    }

    public String issueCard(String userId, String accountId) {
        try {
            return webClient.post()
                    .uri("http://card-service/cards")
                    .bodyValue("{\"userId\":\"" + userId + "\",\"accountId\":\"" + accountId + "\"}")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (Exception e) {
            logger.error("Failed to issue card for user: {}, account: {}", userId, accountId, e);
            throw new RuntimeException("Card issuance failed", e);
        }
    }

    public boolean sendWelcomeNotification(String userId) {
        try {
            webClient.post()
                    .uri("http://notification-service/notifications/welcome")
                    .bodyValue("{\"userId\":\"" + userId + "\"}")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            return true;
        } catch (Exception e) {
            logger.error("Failed to send welcome notification for user: {}", userId, e);
            return false;
        }
    }

    // Compensation methods
    public void deleteUser(String userId) {
        try {
            webClient.delete()
                    .uri("http://user-service/users/" + userId)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
        } catch (Exception e) {
            logger.error("Failed to delete user: {}", userId, e);
            throw new RuntimeException("User deletion failed", e);
        }
    }

    public void deleteAccount(String accountId) {
        try {
            webClient.delete()
                    .uri("http://account-service/accounts/" + accountId)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
        } catch (Exception e) {
            logger.error("Failed to delete account: {}", accountId, e);
            throw new RuntimeException("Account deletion failed", e);
        }
    }
}