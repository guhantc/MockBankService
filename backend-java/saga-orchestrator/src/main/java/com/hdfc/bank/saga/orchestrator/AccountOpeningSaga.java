package com.hdfc.bank.saga.orchestrator;

import com.hdfc.bank.saga.model.SagaTransaction;
import com.hdfc.bank.saga.service.SagaService;
import com.hdfc.bank.saga.service.ServiceCommunicator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Account Opening SAGA Orchestrator
 * 
 * This SAGA handles the complete account opening process:
 * 1. Create User
 * 2. Create Account
 * 3. Issue Debit Card
 * 4. Send Welcome Notification
 * 
 * Compensation activities are executed in reverse order if any step fails.
 */
@Component
public class AccountOpeningSaga {

    private static final Logger logger = LoggerFactory.getLogger(AccountOpeningSaga.class);

    private final ServiceCommunicator serviceCommunicator;
    private final SagaService sagaService;

    public AccountOpeningSaga(ServiceCommunicator serviceCommunicator, SagaService sagaService) {
        this.serviceCommunicator = serviceCommunicator;
        this.sagaService = sagaService;
    }

    public void executeAccountOpeningSaga(String sagaId, String userPayload) {
        SagaTransaction saga = sagaService.getSagaById(sagaId);
        
        try {
            logger.info("Starting Account Opening SAGA: {}", sagaId);
            
            // Step 1: Create User
            String userId = executeCreateUser(saga, userPayload);
            if (userId == null) {
                throw new RuntimeException("Failed to create user");
            }
            
            // Step 2: Create Account
            String accountId = executeCreateAccount(saga, userId);
            if (accountId == null) {
                compensateCreateUser(saga, userId);
                throw new RuntimeException("Failed to create account");
            }
            
            // Step 3: Issue Debit Card
            String cardId = executeIssueCard(saga, userId, accountId);
            if (cardId == null) {
                compensateCreateAccount(saga, accountId);
                compensateCreateUser(saga, userId);
                throw new RuntimeException("Failed to issue card");
            }
            
            // Step 4: Send Welcome Notification
            boolean notificationSent = executeSendNotification(saga, userId);
            if (!notificationSent) {
                // Notification failure is not critical, log and continue
                logger.warn("Failed to send welcome notification for user: {}", userId);
            }
            
            // Mark SAGA as completed
            sagaService.completeSaga(sagaId);
            logger.info("Account Opening SAGA completed successfully: {}", sagaId);
            
        } catch (Exception e) {
            logger.error("Account Opening SAGA failed: {}", sagaId, e);
            sagaService.failSaga(sagaId, e.getMessage());
        }
    }

    private String executeCreateUser(SagaTransaction saga, String userPayload) {
        try {
            sagaService.addStep(saga.getSagaId(), "CREATE_USER", "STARTED");
            String userId = serviceCommunicator.createUser(userPayload);
            sagaService.updateStep(saga.getSagaId(), "CREATE_USER", "COMPLETED", userId);
            return userId;
        } catch (Exception e) {
            sagaService.updateStep(saga.getSagaId(), "CREATE_USER", "FAILED", e.getMessage());
            return null;
        }
    }

    private String executeCreateAccount(SagaTransaction saga, String userId) {
        try {
            sagaService.addStep(saga.getSagaId(), "CREATE_ACCOUNT", "STARTED");
            String accountId = serviceCommunicator.createAccount(userId);
            sagaService.updateStep(saga.getSagaId(), "CREATE_ACCOUNT", "COMPLETED", accountId);
            return accountId;
        } catch (Exception e) {
            sagaService.updateStep(saga.getSagaId(), "CREATE_ACCOUNT", "FAILED", e.getMessage());
            return null;
        }
    }

    private String executeIssueCard(SagaTransaction saga, String userId, String accountId) {
        try {
            sagaService.addStep(saga.getSagaId(), "ISSUE_CARD", "STARTED");
            String cardId = serviceCommunicator.issueCard(userId, accountId);
            sagaService.updateStep(saga.getSagaId(), "ISSUE_CARD", "COMPLETED", cardId);
            return cardId;
        } catch (Exception e) {
            sagaService.updateStep(saga.getSagaId(), "ISSUE_CARD", "FAILED", e.getMessage());
            return null;
        }
    }

    private boolean executeSendNotification(SagaTransaction saga, String userId) {
        try {
            sagaService.addStep(saga.getSagaId(), "SEND_NOTIFICATION", "STARTED");
            boolean sent = serviceCommunicator.sendWelcomeNotification(userId);
            sagaService.updateStep(saga.getSagaId(), "SEND_NOTIFICATION", "COMPLETED", "Notification sent");
            return sent;
        } catch (Exception e) {
            sagaService.updateStep(saga.getSagaId(), "SEND_NOTIFICATION", "FAILED", e.getMessage());
            return false;
        }
    }

    // Compensation Activities
    private void compensateCreateUser(SagaTransaction saga, String userId) {
        try {
            sagaService.addStep(saga.getSagaId(), "COMPENSATE_USER", "STARTED");
            serviceCommunicator.deleteUser(userId);
            sagaService.updateStep(saga.getSagaId(), "COMPENSATE_USER", "COMPLETED", "User deleted");
        } catch (Exception e) {
            sagaService.updateStep(saga.getSagaId(), "COMPENSATE_USER", "FAILED", e.getMessage());
        }
    }

    private void compensateCreateAccount(SagaTransaction saga, String accountId) {
        try {
            sagaService.addStep(saga.getSagaId(), "COMPENSATE_ACCOUNT", "STARTED");
            serviceCommunicator.deleteAccount(accountId);
            sagaService.updateStep(saga.getSagaId(), "COMPENSATE_ACCOUNT", "COMPLETED", "Account deleted");
        } catch (Exception e) {
            sagaService.updateStep(saga.getSagaId(), "COMPENSATE_ACCOUNT", "FAILED", e.getMessage());
        }
    }
}