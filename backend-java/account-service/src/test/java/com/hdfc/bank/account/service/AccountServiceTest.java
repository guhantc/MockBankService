package com.hdfc.bank.account.service;

import com.hdfc.bank.account.dto.AccountCreateRequest;
import com.hdfc.bank.account.dto.AccountResponse;
import com.hdfc.bank.account.model.Account;
import com.hdfc.bank.account.repository.AccountRepository;
import com.hdfc.bank.account.util.AccountMapper;
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
 * Unit tests for AccountService
 */
@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountMapper accountMapper;

    @InjectMocks
    private AccountService accountService;

    private Account account;
    private AccountResponse accountResponse;
    private AccountCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        account = new Account();
        account.setId(1L);
        account.setAccountId("ACC001");
        account.setUserId("USER001");
        account.setAccountType(Account.AccountType.SAVINGS);
        account.setBalance(BigDecimal.valueOf(10000.00));
        account.setStatus(Account.AccountStatus.ACTIVE);
        account.setCreatedAt(LocalDateTime.now());

        accountResponse = new AccountResponse();
        accountResponse.setAccountId("ACC001");
        accountResponse.setUserId("USER001");
        accountResponse.setAccountType(Account.AccountType.SAVINGS);
        accountResponse.setBalance(BigDecimal.valueOf(10000.00));
        accountResponse.setStatus(Account.AccountStatus.ACTIVE);

        createRequest = new AccountCreateRequest();
        createRequest.setUserId("USER001");
        createRequest.setAccountType(Account.AccountType.SAVINGS);
        createRequest.setInitialDeposit(BigDecimal.valueOf(5000.00));
    }

    @Test
    void createAccount_Success() {
        // Given
        when(accountRepository.save(any(Account.class))).thenReturn(account);
        when(accountMapper.toResponse(account)).thenReturn(accountResponse);

        // When
        AccountResponse result = accountService.createAccount(createRequest);

        // Then
        assertNotNull(result);
        assertEquals("ACC001", result.getAccountId());
        assertEquals("USER001", result.getUserId());
        assertEquals(Account.AccountType.SAVINGS, result.getAccountType());
        verify(accountRepository).save(any(Account.class));
        verify(accountMapper).toResponse(account);
    }

    @Test
    void getAccountById_Success() {
        // Given
        when(accountRepository.findByAccountId("ACC001")).thenReturn(Optional.of(account));
        when(accountMapper.toResponse(account)).thenReturn(accountResponse);

        // When
        AccountResponse result = accountService.getAccountById("ACC001");

        // Then
        assertNotNull(result);
        assertEquals("ACC001", result.getAccountId());
        verify(accountRepository).findByAccountId("ACC001");
        verify(accountMapper).toResponse(account);
    }

    @Test
    void getAccountById_NotFound() {
        // Given
        when(accountRepository.findByAccountId("INVALID")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> accountService.getAccountById("INVALID"));
        verify(accountRepository).findByAccountId("INVALID");
        verify(accountMapper, never()).toResponse(any());
    }

    @Test
    void getAccountsByUserId_Success() {
        // Given
        List<Account> accounts = Arrays.asList(account);
        when(accountRepository.findByUserId("USER001")).thenReturn(accounts);
        when(accountMapper.toResponse(account)).thenReturn(accountResponse);

        // When
        List<AccountResponse> result = accountService.getAccountsByUserId("USER001");

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("ACC001", result.get(0).getAccountId());
        verify(accountRepository).findByUserId("USER001");
        verify(accountMapper).toResponse(account);
    }

    @Test
    void updateBalance_Credit_Success() {
        // Given
        BigDecimal creditAmount = BigDecimal.valueOf(1000.00);
        BigDecimal expectedBalance = BigDecimal.valueOf(11000.00);
        when(accountRepository.findByAccountId("ACC001")).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        // When
        accountService.updateBalance("ACC001", creditAmount, Account.TransactionType.CREDIT);

        // Then
        verify(accountRepository).findByAccountId("ACC001");
        verify(accountRepository).save(argThat(acc -> acc.getBalance().equals(expectedBalance)));
    }

    @Test
    void updateBalance_Debit_Success() {
        // Given
        BigDecimal debitAmount = BigDecimal.valueOf(2000.00);
        BigDecimal expectedBalance = BigDecimal.valueOf(8000.00);
        when(accountRepository.findByAccountId("ACC001")).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        // When
        accountService.updateBalance("ACC001", debitAmount, Account.TransactionType.DEBIT);

        // Then
        verify(accountRepository).findByAccountId("ACC001");
        verify(accountRepository).save(argThat(acc -> acc.getBalance().equals(expectedBalance)));
    }

    @Test
    void updateBalance_InsufficientFunds() {
        // Given
        BigDecimal debitAmount = BigDecimal.valueOf(15000.00); // More than balance
        when(accountRepository.findByAccountId("ACC001")).thenReturn(Optional.of(account));

        // When & Then
        assertThrows(RuntimeException.class, 
            () -> accountService.updateBalance("ACC001", debitAmount, Account.TransactionType.DEBIT));
        verify(accountRepository).findByAccountId("ACC001");
        verify(accountRepository, never()).save(any());
    }

    @Test
    void checkBalance_Success() {
        // Given
        when(accountRepository.findByAccountId("ACC001")).thenReturn(Optional.of(account));

        // When
        BigDecimal result = accountService.checkBalance("ACC001");

        // Then
        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(10000.00), result);
        verify(accountRepository).findByAccountId("ACC001");
    }

    @Test
    void freezeAccount_Success() {
        // Given
        when(accountRepository.findByAccountId("ACC001")).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        // When
        accountService.freezeAccount("ACC001");

        // Then
        verify(accountRepository).findByAccountId("ACC001");
        verify(accountRepository).save(argThat(acc -> acc.getStatus() == Account.AccountStatus.FROZEN));
    }

    @Test
    void activateAccount_Success() {
        // Given
        account.setStatus(Account.AccountStatus.FROZEN);
        when(accountRepository.findByAccountId("ACC001")).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        // When
        accountService.activateAccount("ACC001");

        // Then
        verify(accountRepository).findByAccountId("ACC001");
        verify(accountRepository).save(argThat(acc -> acc.getStatus() == Account.AccountStatus.ACTIVE));
    }

    @Test
    void closeAccount_Success() {
        // Given
        when(accountRepository.findByAccountId("ACC001")).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        // When
        accountService.closeAccount("ACC001");

        // Then
        verify(accountRepository).findByAccountId("ACC001");
        verify(accountRepository).save(argThat(acc -> acc.getStatus() == Account.AccountStatus.CLOSED));
    }

    @Test
    void transferFunds_Success() {
        // Given
        Account sourceAccount = new Account();
        sourceAccount.setAccountId("ACC001");
        sourceAccount.setBalance(BigDecimal.valueOf(10000.00));
        sourceAccount.setStatus(Account.AccountStatus.ACTIVE);

        Account targetAccount = new Account();
        targetAccount.setAccountId("ACC002");
        targetAccount.setBalance(BigDecimal.valueOf(5000.00));
        targetAccount.setStatus(Account.AccountStatus.ACTIVE);

        BigDecimal transferAmount = BigDecimal.valueOf(2000.00);

        when(accountRepository.findByAccountId("ACC001")).thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findByAccountId("ACC002")).thenReturn(Optional.of(targetAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(sourceAccount, targetAccount);

        // When
        accountService.transferFunds("ACC001", "ACC002", transferAmount);

        // Then
        verify(accountRepository, times(2)).findByAccountId(anyString());
        verify(accountRepository, times(2)).save(any(Account.class));
    }

    @Test
    void transferFunds_InsufficientBalance() {
        // Given
        Account sourceAccount = new Account();
        sourceAccount.setAccountId("ACC001");
        sourceAccount.setBalance(BigDecimal.valueOf(1000.00)); // Less than transfer amount
        sourceAccount.setStatus(Account.AccountStatus.ACTIVE);

        BigDecimal transferAmount = BigDecimal.valueOf(2000.00);

        when(accountRepository.findByAccountId("ACC001")).thenReturn(Optional.of(sourceAccount));

        // When & Then
        assertThrows(RuntimeException.class, 
            () -> accountService.transferFunds("ACC001", "ACC002", transferAmount));
        verify(accountRepository).findByAccountId("ACC001");
        verify(accountRepository, never()).save(any());
    }
}