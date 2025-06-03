package com.hdfc.bank.saga.service;

import com.hdfc.bank.saga.model.SagaStep;
import com.hdfc.bank.saga.model.SagaTransaction;
import com.hdfc.bank.saga.repository.SagaStepRepository;
import com.hdfc.bank.saga.repository.SagaTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for SagaService
 */
@ExtendWith(MockitoExtension.class)
class SagaServiceTest {

    @Mock
    private SagaTransactionRepository sagaTransactionRepository;

    @Mock
    private SagaStepRepository sagaStepRepository;

    @InjectMocks
    private SagaService sagaService;

    private SagaTransaction sagaTransaction;
    private SagaStep sagaStep;

    @BeforeEach
    void setUp() {
        sagaTransaction = new SagaTransaction();
        sagaTransaction.setId(1L);
        sagaTransaction.setSagaId("SAGA001");
        sagaTransaction.setSagaType(SagaTransaction.SagaType.ACCOUNT_OPENING);
        sagaTransaction.setUserId("USER001");
        sagaTransaction.setStatus(SagaTransaction.SagaStatus.STARTED);
        sagaTransaction.setCreatedAt(LocalDateTime.now());
        sagaTransaction.setUpdatedAt(LocalDateTime.now());
        sagaTransaction.setPayload("{\"userId\":\"USER001\"}");

        sagaStep = new SagaStep();
        sagaStep.setId(1L);
        sagaStep.setSagaTransaction(sagaTransaction);
        sagaStep.setStepName("CREATE_USER");
        sagaStep.setStatus(SagaStep.StepStatus.COMPLETED);
        sagaStep.setCreatedAt(LocalDateTime.now());
        sagaStep.setUpdatedAt(LocalDateTime.now());
        sagaStep.setResult("USER001");
    }

    @Test
    void createSaga_Success() {
        // Given
        when(sagaTransactionRepository.save(any(SagaTransaction.class))).thenReturn(sagaTransaction);

        // When
        SagaTransaction result = sagaService.createSaga(
                SagaTransaction.SagaType.ACCOUNT_OPENING, 
                "USER001", 
                "{\"userId\":\"USER001\"}"
        );

        // Then
        assertNotNull(result);
        assertEquals(SagaTransaction.SagaType.ACCOUNT_OPENING, result.getSagaType());
        assertEquals("USER001", result.getUserId());
        assertEquals(SagaTransaction.SagaStatus.STARTED, result.getStatus());
        verify(sagaTransactionRepository).save(any(SagaTransaction.class));
    }

    @Test
    void getSagaById_Success() {
        // Given
        when(sagaTransactionRepository.findBySagaId("SAGA001")).thenReturn(Optional.of(sagaTransaction));

        // When
        SagaTransaction result = sagaService.getSagaById("SAGA001");

        // Then
        assertNotNull(result);
        assertEquals("SAGA001", result.getSagaId());
        verify(sagaTransactionRepository).findBySagaId("SAGA001");
    }

    @Test
    void getSagaById_NotFound() {
        // Given
        when(sagaTransactionRepository.findBySagaId("INVALID")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> sagaService.getSagaById("INVALID"));
        verify(sagaTransactionRepository).findBySagaId("INVALID");
    }

    @Test
    void addStep_Success() {
        // Given
        when(sagaTransactionRepository.findBySagaId("SAGA001")).thenReturn(Optional.of(sagaTransaction));
        when(sagaStepRepository.save(any(SagaStep.class))).thenReturn(sagaStep);

        // When
        sagaService.addStep("SAGA001", "CREATE_USER", "STARTED");

        // Then
        verify(sagaTransactionRepository).findBySagaId("SAGA001");
        verify(sagaStepRepository).save(argThat(step -> 
                step.getStepName().equals("CREATE_USER") && 
                step.getStatus() == SagaStep.StepStatus.STARTED
        ));
    }

    @Test
    void updateStep_Success() {
        // Given
        when(sagaTransactionRepository.findBySagaId("SAGA001")).thenReturn(Optional.of(sagaTransaction));
        when(sagaStepRepository.findBySagaTransactionAndStepName(sagaTransaction, "CREATE_USER"))
                .thenReturn(Optional.of(sagaStep));
        when(sagaStepRepository.save(any(SagaStep.class))).thenReturn(sagaStep);

        // When
        sagaService.updateStep("SAGA001", "CREATE_USER", "COMPLETED", "USER001");

        // Then
        verify(sagaTransactionRepository).findBySagaId("SAGA001");
        verify(sagaStepRepository).findBySagaTransactionAndStepName(sagaTransaction, "CREATE_USER");
        verify(sagaStepRepository).save(argThat(step -> 
                step.getStatus() == SagaStep.StepStatus.COMPLETED &&
                step.getResult().equals("USER001")
        ));
    }

    @Test
    void completeSaga_Success() {
        // Given
        when(sagaTransactionRepository.findBySagaId("SAGA001")).thenReturn(Optional.of(sagaTransaction));
        when(sagaTransactionRepository.save(any(SagaTransaction.class))).thenReturn(sagaTransaction);

        // When
        sagaService.completeSaga("SAGA001");

        // Then
        verify(sagaTransactionRepository).findBySagaId("SAGA001");
        verify(sagaTransactionRepository).save(argThat(saga -> 
                saga.getStatus() == SagaTransaction.SagaStatus.COMPLETED &&
                saga.getCompletedAt() != null
        ));
    }

    @Test
    void failSaga_Success() {
        // Given
        String errorMessage = "Account creation failed";
        when(sagaTransactionRepository.findBySagaId("SAGA001")).thenReturn(Optional.of(sagaTransaction));
        when(sagaTransactionRepository.save(any(SagaTransaction.class))).thenReturn(sagaTransaction);

        // When
        sagaService.failSaga("SAGA001", errorMessage);

        // Then
        verify(sagaTransactionRepository).findBySagaId("SAGA001");
        verify(sagaTransactionRepository).save(argThat(saga -> 
                saga.getStatus() == SagaTransaction.SagaStatus.FAILED &&
                saga.getErrorMessage().equals(errorMessage)
        ));
    }

    @Test
    void getSagasByUserId_Success() {
        // Given
        List<SagaTransaction> sagas = Arrays.asList(sagaTransaction);
        when(sagaTransactionRepository.findByUserId("USER001")).thenReturn(sagas);

        // When
        List<SagaTransaction> result = sagaService.getSagasByUserId("USER001");

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("SAGA001", result.get(0).getSagaId());
        verify(sagaTransactionRepository).findByUserId("USER001");
    }

    @Test
    void getSagasByStatus_Success() {
        // Given
        List<SagaTransaction> sagas = Arrays.asList(sagaTransaction);
        when(sagaTransactionRepository.findByStatus(SagaTransaction.SagaStatus.STARTED)).thenReturn(sagas);

        // When
        List<SagaTransaction> result = sagaService.getSagasByStatus(SagaTransaction.SagaStatus.STARTED);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(SagaTransaction.SagaStatus.STARTED, result.get(0).getStatus());
        verify(sagaTransactionRepository).findByStatus(SagaTransaction.SagaStatus.STARTED);
    }

    @Test
    void getStepsBySagaId_Success() {
        // Given
        List<SagaStep> steps = Arrays.asList(sagaStep);
        when(sagaTransactionRepository.findBySagaId("SAGA001")).thenReturn(Optional.of(sagaTransaction));
        when(sagaStepRepository.findBySagaTransactionOrderByCreatedAt(sagaTransaction)).thenReturn(steps);

        // When
        List<SagaStep> result = sagaService.getStepsBySagaId("SAGA001");

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("CREATE_USER", result.get(0).getStepName());
        verify(sagaTransactionRepository).findBySagaId("SAGA001");
        verify(sagaStepRepository).findBySagaTransactionOrderByCreatedAt(sagaTransaction);
    }

    @Test
    void isStepCompleted_True() {
        // Given
        when(sagaTransactionRepository.findBySagaId("SAGA001")).thenReturn(Optional.of(sagaTransaction));
        when(sagaStepRepository.findBySagaTransactionAndStepName(sagaTransaction, "CREATE_USER"))
                .thenReturn(Optional.of(sagaStep));

        // When
        boolean result = sagaService.isStepCompleted("SAGA001", "CREATE_USER");

        // Then
        assertTrue(result);
        verify(sagaTransactionRepository).findBySagaId("SAGA001");
        verify(sagaStepRepository).findBySagaTransactionAndStepName(sagaTransaction, "CREATE_USER");
    }

    @Test
    void isStepCompleted_False() {
        // Given
        sagaStep.setStatus(SagaStep.StepStatus.STARTED);
        when(sagaTransactionRepository.findBySagaId("SAGA001")).thenReturn(Optional.of(sagaTransaction));
        when(sagaStepRepository.findBySagaTransactionAndStepName(sagaTransaction, "CREATE_USER"))
                .thenReturn(Optional.of(sagaStep));

        // When
        boolean result = sagaService.isStepCompleted("SAGA001", "CREATE_USER");

        // Then
        assertFalse(result);
    }

    @Test
    void compensateSaga_Success() {
        // Given
        sagaTransaction.setStatus(SagaTransaction.SagaStatus.FAILED);
        when(sagaTransactionRepository.findBySagaId("SAGA001")).thenReturn(Optional.of(sagaTransaction));
        when(sagaTransactionRepository.save(any(SagaTransaction.class))).thenReturn(sagaTransaction);

        // When
        sagaService.compensateSaga("SAGA001");

        // Then
        verify(sagaTransactionRepository).findBySagaId("SAGA001");
        verify(sagaTransactionRepository).save(argThat(saga -> 
                saga.getStatus() == SagaTransaction.SagaStatus.COMPENSATING
        ));
    }

    @Test
    void markSagaCompensated_Success() {
        // Given
        sagaTransaction.setStatus(SagaTransaction.SagaStatus.COMPENSATING);
        when(sagaTransactionRepository.findBySagaId("SAGA001")).thenReturn(Optional.of(sagaTransaction));
        when(sagaTransactionRepository.save(any(SagaTransaction.class))).thenReturn(sagaTransaction);

        // When
        sagaService.markSagaCompensated("SAGA001");

        // Then
        verify(sagaTransactionRepository).findBySagaId("SAGA001");
        verify(sagaTransactionRepository).save(argThat(saga -> 
                saga.getStatus() == SagaTransaction.SagaStatus.COMPENSATED
        ));
    }
}