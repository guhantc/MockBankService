package com.hdfc.bank.saga.orchestrator;

import com.hdfc.bank.saga.model.SagaTransaction;
import com.hdfc.bank.saga.service.SagaService;
import com.hdfc.bank.saga.service.ServiceCommunicator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AccountOpeningSaga
 */
@ExtendWith(MockitoExtension.class)
class AccountOpeningSagaTest {

    @Mock
    private ServiceCommunicator serviceCommunicator;

    @Mock
    private SagaService sagaService;

    @InjectMocks
    private AccountOpeningSaga accountOpeningSaga;

    private SagaTransaction sagaTransaction;
    private String userPayload;

    @BeforeEach
    void setUp() {
        sagaTransaction = new SagaTransaction();
        sagaTransaction.setSagaId("SAGA001");
        sagaTransaction.setSagaType(SagaTransaction.SagaType.ACCOUNT_OPENING);
        sagaTransaction.setUserId("USER001");
        sagaTransaction.setStatus(SagaTransaction.SagaStatus.STARTED);

        userPayload = """
            {
                "firstName": "John",
                "lastName": "Doe",
                "email": "john.doe@example.com",
                "phoneNumber": "9876543210"
            }
            """;
    }

    @Test
    void executeAccountOpeningSaga_Success() {
        // Given
        when(sagaService.getSagaById("SAGA001")).thenReturn(sagaTransaction);
        when(serviceCommunicator.createUser(userPayload)).thenReturn("USER001");
        when(serviceCommunicator.createAccount("USER001")).thenReturn("ACC001");
        when(serviceCommunicator.issueCard("USER001", "ACC001")).thenReturn("CARD001");
        when(serviceCommunicator.sendWelcomeNotification("USER001")).thenReturn(true);
        doNothing().when(sagaService).addStep(anyString(), anyString(), anyString());
        doNothing().when(sagaService).updateStep(anyString(), anyString(), anyString(), anyString());
        doNothing().when(sagaService).completeSaga("SAGA001");

        // When
        accountOpeningSaga.executeAccountOpeningSaga("SAGA001", userPayload);

        // Then
        verify(sagaService).getSagaById("SAGA001");
        verify(serviceCommunicator).createUser(userPayload);
        verify(serviceCommunicator).createAccount("USER001");
        verify(serviceCommunicator).issueCard("USER001", "ACC001");
        verify(serviceCommunicator).sendWelcomeNotification("USER001");
        verify(sagaService).completeSaga("SAGA001");
        
        // Verify all steps were added and updated
        verify(sagaService, times(4)).addStep(eq("SAGA001"), anyString(), eq("STARTED"));
        verify(sagaService, times(4)).updateStep(eq("SAGA001"), anyString(), eq("COMPLETED"), anyString());
    }

    @Test
    void executeAccountOpeningSaga_UserCreationFails() {
        // Given
        when(sagaService.getSagaById("SAGA001")).thenReturn(sagaTransaction);
        when(serviceCommunicator.createUser(userPayload)).thenThrow(new RuntimeException("User creation failed"));
        doNothing().when(sagaService).addStep(anyString(), anyString(), anyString());
        doNothing().when(sagaService).updateStep(anyString(), anyString(), anyString(), anyString());
        doNothing().when(sagaService).failSaga(anyString(), anyString());

        // When
        accountOpeningSaga.executeAccountOpeningSaga("SAGA001", userPayload);

        // Then
        verify(sagaService).getSagaById("SAGA001");
        verify(serviceCommunicator).createUser(userPayload);
        verify(serviceCommunicator, never()).createAccount(anyString());
        verify(serviceCommunicator, never()).issueCard(anyString(), anyString());
        verify(serviceCommunicator, never()).sendWelcomeNotification(anyString());
        verify(sagaService).failSaga(eq("SAGA001"), anyString());
        verify(sagaService, never()).completeSaga(anyString());
    }

    @Test
    void executeAccountOpeningSaga_AccountCreationFails_WithCompensation() {
        // Given
        when(sagaService.getSagaById("SAGA001")).thenReturn(sagaTransaction);
        when(serviceCommunicator.createUser(userPayload)).thenReturn("USER001");
        when(serviceCommunicator.createAccount("USER001")).thenThrow(new RuntimeException("Account creation failed"));
        doNothing().when(serviceCommunicator).deleteUser("USER001");
        doNothing().when(sagaService).addStep(anyString(), anyString(), anyString());
        doNothing().when(sagaService).updateStep(anyString(), anyString(), anyString(), anyString());
        doNothing().when(sagaService).failSaga(anyString(), anyString());

        // When
        accountOpeningSaga.executeAccountOpeningSaga("SAGA001", userPayload);

        // Then
        verify(sagaService).getSagaById("SAGA001");
        verify(serviceCommunicator).createUser(userPayload);
        verify(serviceCommunicator).createAccount("USER001");
        verify(serviceCommunicator).deleteUser("USER001"); // Compensation
        verify(serviceCommunicator, never()).issueCard(anyString(), anyString());
        verify(serviceCommunicator, never()).sendWelcomeNotification(anyString());
        verify(sagaService).failSaga(eq("SAGA001"), anyString());
    }

    @Test
    void executeAccountOpeningSaga_CardIssuanceFails_WithCompensation() {
        // Given
        when(sagaService.getSagaById("SAGA001")).thenReturn(sagaTransaction);
        when(serviceCommunicator.createUser(userPayload)).thenReturn("USER001");
        when(serviceCommunicator.createAccount("USER001")).thenReturn("ACC001");
        when(serviceCommunicator.issueCard("USER001", "ACC001")).thenThrow(new RuntimeException("Card issuance failed"));
        doNothing().when(serviceCommunicator).deleteUser("USER001");
        doNothing().when(serviceCommunicator).deleteAccount("ACC001");
        doNothing().when(sagaService).addStep(anyString(), anyString(), anyString());
        doNothing().when(sagaService).updateStep(anyString(), anyString(), anyString(), anyString());
        doNothing().when(sagaService).failSaga(anyString(), anyString());

        // When
        accountOpeningSaga.executeAccountOpeningSaga("SAGA001", userPayload);

        // Then
        verify(sagaService).getSagaById("SAGA001");
        verify(serviceCommunicator).createUser(userPayload);
        verify(serviceCommunicator).createAccount("USER001");
        verify(serviceCommunicator).issueCard("USER001", "ACC001");
        verify(serviceCommunicator).deleteAccount("ACC001"); // Compensation
        verify(serviceCommunicator).deleteUser("USER001"); // Compensation
        verify(serviceCommunicator, never()).sendWelcomeNotification(anyString());
        verify(sagaService).failSaga(eq("SAGA001"), anyString());
    }

    @Test
    void executeAccountOpeningSaga_NotificationFails_ContinuesExecution() {
        // Given
        when(sagaService.getSagaById("SAGA001")).thenReturn(sagaTransaction);
        when(serviceCommunicator.createUser(userPayload)).thenReturn("USER001");
        when(serviceCommunicator.createAccount("USER001")).thenReturn("ACC001");
        when(serviceCommunicator.issueCard("USER001", "ACC001")).thenReturn("CARD001");
        when(serviceCommunicator.sendWelcomeNotification("USER001")).thenReturn(false); // Notification fails
        doNothing().when(sagaService).addStep(anyString(), anyString(), anyString());
        doNothing().when(sagaService).updateStep(anyString(), anyString(), anyString(), anyString());
        doNothing().when(sagaService).completeSaga("SAGA001");

        // When
        accountOpeningSaga.executeAccountOpeningSaga("SAGA001", userPayload);

        // Then
        verify(sagaService).getSagaById("SAGA001");
        verify(serviceCommunicator).createUser(userPayload);
        verify(serviceCommunicator).createAccount("USER001");
        verify(serviceCommunicator).issueCard("USER001", "ACC001");
        verify(serviceCommunicator).sendWelcomeNotification("USER001");
        verify(sagaService).completeSaga("SAGA001"); // SAGA still completes despite notification failure
        
        // No compensation should be called for notification failure
        verify(serviceCommunicator, never()).deleteUser(anyString());
        verify(serviceCommunicator, never()).deleteAccount(anyString());
    }

    @Test
    void executeAccountOpeningSaga_CompensationAlsoFails() {
        // Given
        when(sagaService.getSagaById("SAGA001")).thenReturn(sagaTransaction);
        when(serviceCommunicator.createUser(userPayload)).thenReturn("USER001");
        when(serviceCommunicator.createAccount("USER001")).thenThrow(new RuntimeException("Account creation failed"));
        doThrow(new RuntimeException("Compensation failed")).when(serviceCommunicator).deleteUser("USER001");
        doNothing().when(sagaService).addStep(anyString(), anyString(), anyString());
        doNothing().when(sagaService).updateStep(anyString(), anyString(), anyString(), anyString());
        doNothing().when(sagaService).failSaga(anyString(), anyString());

        // When
        accountOpeningSaga.executeAccountOpeningSaga("SAGA001", userPayload);

        // Then
        verify(sagaService).getSagaById("SAGA001");
        verify(serviceCommunicator).createUser(userPayload);
        verify(serviceCommunicator).createAccount("USER001");
        verify(serviceCommunicator).deleteUser("USER001"); // Compensation attempted
        verify(sagaService).failSaga(eq("SAGA001"), anyString());
        
        // Verify compensation failure is recorded
        verify(sagaService).updateStep(eq("SAGA001"), eq("COMPENSATE_USER"), eq("FAILED"), anyString());
    }
}