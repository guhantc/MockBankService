package com.hdfc.bank.saga.integration;

import com.hdfc.bank.saga.model.SagaTransaction;
import com.hdfc.bank.saga.repository.SagaTransactionRepository;
import com.hdfc.bank.saga.service.SagaService;
import com.hdfc.bank.test.util.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for SAGA Orchestrator
 */
class SagaIntegrationTest extends IntegrationTestBase {

    @Autowired
    private SagaTransactionRepository sagaTransactionRepository;

    @Autowired
    private SagaService sagaService;

    @Test
    void createAccountOpeningSaga_Success() throws Exception {
        // Given
        String sagaRequest = """
            {
                "sagaType": "ACCOUNT_OPENING",
                "userId": "USER001",
                "payload": {
                    "firstName": "John",
                    "lastName": "Doe",
                    "email": "john.doe@test.com",
                    "phoneNumber": "9876543210",
                    "accountType": "SAVINGS",
                    "initialDeposit": 5000.00
                }
            }
            """;

        // When & Then
        mockMvc.perform(post("/saga/account-opening")
                .contentType(MediaType.APPLICATION_JSON)
                .content(sagaRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sagaType").value("ACCOUNT_OPENING"))
                .andExpected(jsonPath("$.status").value("STARTED"))
                .andExpected(jsonPath("$.userId").value("USER001"));
    }

    @Test
    void getSagaById_Success() throws Exception {
        // Given
        SagaTransaction saga = sagaService.createSaga(
                SagaTransaction.SagaType.ACCOUNT_OPENING,
                "USER001",
                "{\"test\": \"data\"}"
        );

        // When & Then
        mockMvc.perform(get("/saga/" + saga.getSagaId()))
                .andExpect(status().isOk())
                .andExpected(jsonPath("$.sagaId").value(saga.getSagaId()))
                .andExpected(jsonPath("$.sagaType").value("ACCOUNT_OPENING"));
    }

    @Test
    void getSagasByUserId_Success() throws Exception {
        // Given
        sagaService.createSaga(
                SagaTransaction.SagaType.ACCOUNT_OPENING,
                "USER001",
                "{\"test\": \"data1\"}"
        );
        sagaService.createSaga(
                SagaTransaction.SagaType.LOAN_PROCESSING,
                "USER001",
                "{\"test\": \"data2\"}"
        );

        // When & Then
        mockMvc.perform(get("/saga/user/USER001"))
                .andExpect(status().isOk())
                .andExpected(jsonPath("$").isArray())
                .andExpected(jsonPath("$.length()").value(2));
    }

    @Test
    void getSagaSteps_Success() throws Exception {
        // Given
        SagaTransaction saga = sagaService.createSaga(
                SagaTransaction.SagaType.ACCOUNT_OPENING,
                "USER001",
                "{\"test\": \"data\"}"
        );

        // Add some steps
        sagaService.addStep(saga.getSagaId(), "CREATE_USER", "STARTED");
        sagaService.updateStep(saga.getSagaId(), "CREATE_USER", "COMPLETED", "USER001");
        sagaService.addStep(saga.getSagaId(), "CREATE_ACCOUNT", "STARTED");

        // When & Then
        mockMvc.perform(get("/saga/" + saga.getSagaId() + "/steps"))
                .andExpect(status().isOk())
                .andExpected(jsonPath("$").isArray())
                .andExpected(jsonPath("$.length()").value(2))
                .andExpected(jsonPath("$[0].stepName").value("CREATE_USER"))
                .andExpected(jsonPath("$[0].status").value("COMPLETED"));
    }

    @Test
    void compensateSaga_Success() throws Exception {
        // Given
        SagaTransaction saga = sagaService.createSaga(
                SagaTransaction.SagaType.ACCOUNT_OPENING,
                "USER001",
                "{\"test\": \"data\"}"
        );
        sagaService.failSaga(saga.getSagaId(), "Test failure");

        // When & Then
        mockMvc.perform(post("/saga/" + saga.getSagaId() + "/compensate"))
                .andExpect(status().isOk());

        // Verify compensation started
        mockMvc.perform(get("/saga/" + saga.getSagaId()))
                .andExpect(status().isOk())
                .andExpected(jsonPath("$.status").value("COMPENSATING"));
    }

    @Test
    void loanProcessingSaga_Success() throws Exception {
        // Given
        String sagaRequest = """
            {
                "sagaType": "LOAN_PROCESSING",
                "userId": "USER001",
                "payload": {
                    "loanType": "HOME_LOAN",
                    "principalAmount": 500000.00,
                    "tenureMonths": 240,
                    "annualIncome": 1200000.00,
                    "accountId": "ACC001"
                }
            }
            """;

        // When & Then
        mockMvc.perform(post("/saga/loan-processing")
                .contentType(MediaType.APPLICATION_JSON)
                .content(sagaRequest))
                .andExpect(status().isOk())
                .andExpected(jsonPath("$.sagaType").value("LOAN_PROCESSING"))
                .andExpected(jsonPath("$.status").value("STARTED"));
    }

    @Test
    void fundTransferSaga_Success() throws Exception {
        // Given
        String sagaRequest = """
            {
                "sagaType": "FUND_TRANSFER",
                "userId": "USER001",
                "payload": {
                    "sourceAccountId": "ACC001",
                    "targetAccountId": "ACC002",
                    "amount": 10000.00,
                    "description": "Transfer to friend",
                    "pin": "1234"
                }
            }
            """;

        // When & Then
        mockMvc.perform(post("/saga/fund-transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(sagaRequest))
                .andExpect(status().isOk())
                .andExpected(jsonPath("$.sagaType").value("FUND_TRANSFER"))
                .andExpected(jsonPath("$.status").value("STARTED"));
    }

    @Test
    void getSagasByStatus_Success() throws Exception {
        // Given
        SagaTransaction completedSaga = sagaService.createSaga(
                SagaTransaction.SagaType.ACCOUNT_OPENING,
                "USER001",
                "{\"test\": \"completed\"}"
        );
        sagaService.completeSaga(completedSaga.getSagaId());

        SagaTransaction failedSaga = sagaService.createSaga(
                SagaTransaction.SagaType.LOAN_PROCESSING,
                "USER002",
                "{\"test\": \"failed\"}"
        );
        sagaService.failSaga(failedSaga.getSagaId(), "Test failure");

        // When & Then
        mockMvc.perform(get("/saga/status/COMPLETED"))
                .andExpect(status().isOk())
                .andExpected(jsonPath("$").isArray())
                .andExpected(jsonPath("$[?(@.status == 'COMPLETED')]").exists());

        mockMvc.perform(get("/saga/status/FAILED"))
                .andExpect(status().isOk())
                .andExpected(jsonPath("$").isArray())
                .andExpected(jsonPath("$[?(@.status == 'FAILED')]").exists());
    }

    @Test
    void sagaTimeout_Handling() throws Exception {
        // Given
        SagaTransaction saga = sagaService.createSaga(
                SagaTransaction.SagaType.ACCOUNT_OPENING,
                "USER001",
                "{\"test\": \"timeout\"}"
        );

        // Simulate long-running saga
        sagaService.addStep(saga.getSagaId(), "CREATE_USER", "STARTED");
        
        // When & Then
        mockMvc.perform(get("/saga/timeout"))
                .andExpect(status().isOk());
    }

    @Test
    void sagaMetrics_Success() throws Exception {
        // Given - Create sagas with different statuses
        SagaTransaction completedSaga = sagaService.createSaga(
                SagaTransaction.SagaType.ACCOUNT_OPENING,
                "USER001",
                "{\"test\": \"metrics1\"}"
        );
        sagaService.completeSaga(completedSaga.getSagaId());

        SagaTransaction failedSaga = sagaService.createSaga(
                SagaTransaction.SagaType.LOAN_PROCESSING,
                "USER002",
                "{\"test\": \"metrics2\"}"
        );
        sagaService.failSaga(failedSaga.getSagaId(), "Test failure");

        // When & Then
        mockMvc.perform(get("/saga/metrics"))
                .andExpect(status().isOk())
                .andExpected(jsonPath("$.totalSagas").exists())
                .andExpected(jsonPath("$.completedSagas").exists())
                .andExpected(jsonPath("$.failedSagas").exists())
                .andExpected(jsonPath("$.successRate").exists());
    }
}