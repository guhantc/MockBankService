package com.hdfc.bank.transaction.service;

import com.hdfc.bank.transaction.dto.TransactionRequest;
import com.hdfc.bank.transaction.dto.TransactionResponse;
import com.hdfc.bank.transaction.model.Transaction;
import com.hdfc.bank.transaction.repository.TransactionRepository;
import com.hdfc.bank.transaction.util.TransactionMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TransactionService
 */
@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private TransactionMapper transactionMapper;

    @Mock
    private AccountServiceClient accountServiceClient;

    @Mock
    private NotificationServiceClient notificationServiceClient;

    @InjectMocks
    private TransactionService transactionService;

    private Transaction transaction;
    private TransactionResponse transactionResponse;
    private TransactionRequest transactionRequest;

    @BeforeEach
    void setUp() {
        transaction = new Transaction();
        transaction.setId(1L);
        transaction.setTransactionId("TXN001");
        transaction.setSourceAccountId("ACC001");
        transaction.setTargetAccountId("ACC002");
        transaction.setAmount(BigDecimal.valueOf(5000.00));
        transaction.setTransactionType(Transaction.TransactionType.TRANSFER);
        transaction.setStatus(Transaction.TransactionStatus.COMPLETED);
        transaction.setDescription("Transfer to John Doe");
        transaction.setCreatedAt(LocalDateTime.now());

        transactionResponse = new TransactionResponse();
        transactionResponse.setTransactionId("TXN001");
        transactionResponse.setSourceAccountId("ACC001");
        transactionResponse.setTargetAccountId("ACC002");
        transactionResponse.setAmount(BigDecimal.valueOf(5000.00));
        transactionResponse.setTransactionType(Transaction.TransactionType.TRANSFER);
        transactionResponse.setStatus(Transaction.TransactionStatus.COMPLETED);
        transactionResponse.setDescription("Transfer to John Doe");

        transactionRequest = new TransactionRequest();
        transactionRequest.setSourceAccountId("ACC001");
        transactionRequest.setTargetAccountId("ACC002");
        transactionRequest.setAmount(BigDecimal.valueOf(5000.00));
        transactionRequest.setTransactionType(Transaction.TransactionType.TRANSFER);
        transactionRequest.setDescription("Transfer to John Doe");
        transactionRequest.setPin("1234");
    }

    @Test
    void processTransaction_Success() {
        // Given
        when(accountServiceClient.checkBalance("ACC001")).thenReturn(BigDecimal.valueOf(10000.00));
        when(accountServiceClient.validateAccount("ACC002")).thenReturn(true);
        when(accountServiceClient.debitAccount("ACC001", BigDecimal.valueOf(5000.00))).thenReturn(true);
        when(accountServiceClient.creditAccount("ACC002", BigDecimal.valueOf(5000.00))).thenReturn(true);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);
        when(transactionMapper.toResponse(transaction)).thenReturn(transactionResponse);
        doNothing().when(notificationServiceClient).sendTransactionNotification(anyString(), anyString());

        // When
        TransactionResponse result = transactionService.processTransaction(transactionRequest);

        // Then
        assertNotNull(result);
        assertEquals("TXN001", result.getTransactionId());
        assertEquals(Transaction.TransactionStatus.COMPLETED, result.getStatus());
        verify(accountServiceClient).checkBalance("ACC001");
        verify(accountServiceClient).validateAccount("ACC002");
        verify(accountServiceClient).debitAccount("ACC001", BigDecimal.valueOf(5000.00));
        verify(accountServiceClient).creditAccount("ACC002", BigDecimal.valueOf(5000.00));
        verify(transactionRepository).save(any(Transaction.class));
        verify(notificationServiceClient, times(2)).sendTransactionNotification(anyString(), anyString());
    }

    @Test
    void processTransaction_InsufficientBalance() {
        // Given
        when(accountServiceClient.checkBalance("ACC001")).thenReturn(BigDecimal.valueOf(3000.00)); // Less than transfer amount

        // When & Then
        assertThrows(RuntimeException.class, () -> transactionService.processTransaction(transactionRequest));
        verify(accountServiceClient).checkBalance("ACC001");
        verify(accountServiceClient, never()).debitAccount(anyString(), any());
        verify(accountServiceClient, never()).creditAccount(anyString(), any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void processTransaction_InvalidTargetAccount() {
        // Given
        when(accountServiceClient.checkBalance("ACC001")).thenReturn(BigDecimal.valueOf(10000.00));
        when(accountServiceClient.validateAccount("ACC002")).thenReturn(false);

        // When & Then
        assertThrows(RuntimeException.class, () -> transactionService.processTransaction(transactionRequest));
        verify(accountServiceClient).checkBalance("ACC001");
        verify(accountServiceClient).validateAccount("ACC002");
        verify(accountServiceClient, never()).debitAccount(anyString(), any());
        verify(accountServiceClient, never()).creditAccount(anyString(), any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void processTransaction_DebitFails_WithCompensation() {
        // Given
        when(accountServiceClient.checkBalance("ACC001")).thenReturn(BigDecimal.valueOf(10000.00));
        when(accountServiceClient.validateAccount("ACC002")).thenReturn(true);
        when(accountServiceClient.debitAccount("ACC001", BigDecimal.valueOf(5000.00))).thenReturn(false);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        // When & Then
        assertThrows(RuntimeException.class, () -> transactionService.processTransaction(transactionRequest));
        verify(accountServiceClient).debitAccount("ACC001", BigDecimal.valueOf(5000.00));
        verify(accountServiceClient, never()).creditAccount(anyString(), any());
        verify(transactionRepository).save(argThat(txn -> txn.getStatus() == Transaction.TransactionStatus.FAILED));
    }

    @Test
    void processTransaction_CreditFails_WithCompensation() {
        // Given
        when(accountServiceClient.checkBalance("ACC001")).thenReturn(BigDecimal.valueOf(10000.00));
        when(accountServiceClient.validateAccount("ACC002")).thenReturn(true);
        when(accountServiceClient.debitAccount("ACC001", BigDecimal.valueOf(5000.00))).thenReturn(true);
        when(accountServiceClient.creditAccount("ACC002", BigDecimal.valueOf(5000.00))).thenReturn(false);
        when(accountServiceClient.creditAccount("ACC001", BigDecimal.valueOf(5000.00))).thenReturn(true); // Compensation
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        // When & Then
        assertThrows(RuntimeException.class, () -> transactionService.processTransaction(transactionRequest));
        verify(accountServiceClient).debitAccount("ACC001", BigDecimal.valueOf(5000.00));
        verify(accountServiceClient).creditAccount("ACC002", BigDecimal.valueOf(5000.00));
        verify(accountServiceClient).creditAccount("ACC001", BigDecimal.valueOf(5000.00)); // Compensation
        verify(transactionRepository).save(argThat(txn -> txn.getStatus() == Transaction.TransactionStatus.FAILED));
    }

    @Test
    void getTransactionById_Success() {
        // Given
        when(transactionRepository.findByTransactionId("TXN001")).thenReturn(Optional.of(transaction));
        when(transactionMapper.toResponse(transaction)).thenReturn(transactionResponse);

        // When
        TransactionResponse result = transactionService.getTransactionById("TXN001");

        // Then
        assertNotNull(result);
        assertEquals("TXN001", result.getTransactionId());
        verify(transactionRepository).findByTransactionId("TXN001");
        verify(transactionMapper).toResponse(transaction);
    }

    @Test
    void getTransactionsByAccountId_Success() {
        // Given
        List<Transaction> transactions = Arrays.asList(transaction);
        when(transactionRepository.findBySourceAccountIdOrTargetAccountId("ACC001", "ACC001")).thenReturn(transactions);
        when(transactionMapper.toResponse(transaction)).thenReturn(transactionResponse);

        // When
        List<TransactionResponse> result = transactionService.getTransactionsByAccountId("ACC001");

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("TXN001", result.get(0).getTransactionId());
        verify(transactionRepository).findBySourceAccountIdOrTargetAccountId("ACC001", "ACC001");
        verify(transactionMapper).toResponse(transaction);
    }

    @Test
    void getTransactionsByStatus_Success() {
        // Given
        List<Transaction> completedTransactions = Arrays.asList(transaction);
        when(transactionRepository.findByStatus(Transaction.TransactionStatus.COMPLETED)).thenReturn(completedTransactions);
        when(transactionMapper.toResponse(transaction)).thenReturn(transactionResponse);

        // When
        List<TransactionResponse> result = transactionService.getTransactionsByStatus(Transaction.TransactionStatus.COMPLETED);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(transactionRepository).findByStatus(Transaction.TransactionStatus.COMPLETED);
    }

    @Test
    void reverseTransaction_Success() {
        // Given
        when(transactionRepository.findByTransactionId("TXN001")).thenReturn(Optional.of(transaction));
        when(accountServiceClient.debitAccount("ACC002", BigDecimal.valueOf(5000.00))).thenReturn(true);
        when(accountServiceClient.creditAccount("ACC001", BigDecimal.valueOf(5000.00))).thenReturn(true);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        // When
        transactionService.reverseTransaction("TXN001", "Manual reversal");

        // Then
        verify(transactionRepository).findByTransactionId("TXN001");
        verify(accountServiceClient).debitAccount("ACC002", BigDecimal.valueOf(5000.00));
        verify(accountServiceClient).creditAccount("ACC001", BigDecimal.valueOf(5000.00));
        verify(transactionRepository).save(argThat(txn -> txn.getStatus() == Transaction.TransactionStatus.REVERSED));
    }

    @Test
    void validateTransactionLimits_DailyLimitExceeded() {
        // Given
        transactionRequest.setAmount(BigDecimal.valueOf(200000.00)); // Above daily limit
        when(transactionRepository.getDailyTransactionSum("ACC001", any(), any()))
                .thenReturn(BigDecimal.valueOf(150000.00));

        // When & Then
        assertThrows(RuntimeException.class, () -> transactionService.validateTransactionLimits(transactionRequest));
    }

    @Test
    void validateTransactionLimits_MonthlyLimitExceeded() {
        // Given
        transactionRequest.setAmount(BigDecimal.valueOf(50000.00));
        when(transactionRepository.getDailyTransactionSum("ACC001", any(), any()))
                .thenReturn(BigDecimal.valueOf(30000.00));
        when(transactionRepository.getMonthlyTransactionSum("ACC001", any(), any()))
                .thenReturn(BigDecimal.valueOf(950000.00)); // Close to monthly limit

        // When & Then
        assertThrows(RuntimeException.class, () -> transactionService.validateTransactionLimits(transactionRequest));
    }

    @Test
    void processRecurringTransaction_Success() {
        // Given
        when(accountServiceClient.checkBalance("ACC001")).thenReturn(BigDecimal.valueOf(10000.00));
        when(accountServiceClient.validateAccount("ACC002")).thenReturn(true);
        when(accountServiceClient.debitAccount("ACC001", BigDecimal.valueOf(5000.00))).thenReturn(true);
        when(accountServiceClient.creditAccount("ACC002", BigDecimal.valueOf(5000.00))).thenReturn(true);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        // When
        transactionService.processRecurringTransaction("RECURRING001");

        // Then
        verify(transactionRepository).save(any(Transaction.class));
        verify(notificationServiceClient, times(2)).sendTransactionNotification(anyString(), anyString());
    }

    @Test
    void calculateTransactionFee_Success() {
        // Given
        BigDecimal amount = BigDecimal.valueOf(10000.00);
        Transaction.TransactionType type = Transaction.TransactionType.NEFT;

        // When
        BigDecimal fee = transactionService.calculateTransactionFee(amount, type);

        // Then
        assertNotNull(fee);
        assertTrue(fee.compareTo(BigDecimal.ZERO) >= 0);
    }

    @Test
    void getTransactionHistory_Success() {
        // Given
        List<Transaction> transactions = Arrays.asList(transaction);
        when(transactionRepository.findBySourceAccountIdOrTargetAccountIdAndCreatedAtBetween(
                eq("ACC001"), eq("ACC001"), any(), any())).thenReturn(transactions);
        when(transactionMapper.toResponse(transaction)).thenReturn(transactionResponse);

        // When
        List<TransactionResponse> result = transactionService.getTransactionHistory(
                "ACC001", LocalDateTime.now().minusDays(30), LocalDateTime.now());

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(transactionRepository).findBySourceAccountIdOrTargetAccountIdAndCreatedAtBetween(
                eq("ACC001"), eq("ACC001"), any(), any());
    }
}