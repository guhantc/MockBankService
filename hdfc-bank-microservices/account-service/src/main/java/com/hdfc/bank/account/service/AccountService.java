package com.hdfc.bank.account.service;

import com.hdfc.bank.account.dto.AccountDto;
import com.hdfc.bank.account.entity.Account;
import com.hdfc.bank.account.exception.AccountNotFoundException;
import com.hdfc.bank.account.exception.InsufficientBalanceException;
import com.hdfc.bank.account.exception.InvalidAccountOperationException;
import com.hdfc.bank.account.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class AccountService {

    @Autowired
    private AccountRepository accountRepository;

    public AccountDto createAccount(AccountDto accountDto) {
        validateAccountCreation(accountDto);
        
        Account account = convertToEntity(accountDto);
        Account savedAccount = accountRepository.save(account);
        
        return convertToDto(savedAccount);
    }

    @Transactional(readOnly = true)
    public AccountDto getAccountByNumber(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Account not found: " + accountNumber));
        return convertToDto(account);
    }

    @Transactional(readOnly = true)
    public List<AccountDto> getAccountsByCustomerId(String customerId) {
        return accountRepository.findByCustomerId(customerId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AccountDto> getAccountsByType(Account.AccountType accountType) {
        return accountRepository.findByAccountType(accountType).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AccountDto> getAllAccounts() {
        return accountRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BigDecimal getBalance(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Account not found: " + accountNumber));
        return account.getBalance();
    }

    public AccountDto depositMoney(String accountNumber, BigDecimal amount) {
        validateDepositAmount(amount);
        
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Account not found: " + accountNumber));
        
        validateAccountStatus(account);
        
        account.credit(amount);
        Account savedAccount = accountRepository.save(account);
        
        return convertToDto(savedAccount);
    }

    public AccountDto withdrawMoney(String accountNumber, BigDecimal amount) {
        validateWithdrawalAmount(amount);
        
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Account not found: " + accountNumber));
        
        validateAccountStatus(account);
        
        if (!account.canDebit(amount)) {
            throw new InsufficientBalanceException("Insufficient balance or daily limit exceeded");
        }
        
        account.debit(amount);
        Account savedAccount = accountRepository.save(account);
        
        return convertToDto(savedAccount);
    }

    public void transferMoney(String fromAccountNumber, String toAccountNumber, BigDecimal amount) {
        validateTransferAmount(amount);
        
        Account fromAccount = accountRepository.findByAccountNumber(fromAccountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Source account not found: " + fromAccountNumber));
        
        Account toAccount = accountRepository.findByAccountNumber(toAccountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Destination account not found: " + toAccountNumber));
        
        validateAccountStatus(fromAccount);
        validateAccountStatus(toAccount);
        
        if (!fromAccount.canDebit(amount)) {
            throw new InsufficientBalanceException("Insufficient balance or daily limit exceeded in source account");
        }
        
        // Transfer operation
        fromAccount.debit(amount);
        toAccount.credit(amount);
        
        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);
    }

    public AccountDto updateAccountStatus(String accountNumber, Account.AccountStatus status) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Account not found: " + accountNumber));
        
        account.setStatus(status);
        Account savedAccount = accountRepository.save(account);
        
        return convertToDto(savedAccount);
    }

    public AccountDto updateDailyLimit(String accountNumber, BigDecimal newLimit) {
        validateDailyLimit(newLimit);
        
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Account not found: " + accountNumber));
        
        account.setDailyTransactionLimit(newLimit);
        Account savedAccount = accountRepository.save(account);
        
        return convertToDto(savedAccount);
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalBalanceByCustomer(String customerId) {
        BigDecimal totalBalance = accountRepository.getTotalBalanceByCustomer(customerId);
        return totalBalance != null ? totalBalance : BigDecimal.ZERO;
    }

    @Transactional(readOnly = true)
    public int getActiveAccountCount(String customerId) {
        return accountRepository.countActiveAccountsByCustomer(customerId);
    }

    public AccountDto closeAccount(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Account not found: " + accountNumber));
        
        if (account.getBalance().compareTo(BigDecimal.ZERO) > 0) {
            throw new InvalidAccountOperationException("Cannot close account with positive balance");
        }
        
        account.setStatus(Account.AccountStatus.CLOSED);
        Account savedAccount = accountRepository.save(account);
        
        return convertToDto(savedAccount);
    }

    // Validation methods
    private void validateAccountCreation(AccountDto accountDto) {
        if (accountDto.getCustomerId() == null || accountDto.getCustomerId().trim().isEmpty()) {
            throw new IllegalArgumentException("Customer ID is required");
        }
        
        if (accountDto.getAccountType() == null) {
            throw new IllegalArgumentException("Account type is required");
        }
        
        // Check if customer already has this type of account (business rule)
        if (accountRepository.existsByCustomerIdAndAccountType(accountDto.getCustomerId(), accountDto.getAccountType())) {
            throw new InvalidAccountOperationException("Customer already has an account of type: " + accountDto.getAccountType());
        }
    }

    private void validateDepositAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Deposit amount must be positive");
        }
        
        if (amount.compareTo(new BigDecimal("1000000")) > 0) {
            throw new IllegalArgumentException("Single deposit cannot exceed ₹10,00,000");
        }
    }

    private void validateWithdrawalAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be positive");
        }
    }

    private void validateTransferAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transfer amount must be positive");
        }
        
        if (amount.compareTo(new BigDecimal("500000")) > 0) {
            throw new IllegalArgumentException("Single transfer cannot exceed ₹5,00,000");
        }
    }

    private void validateDailyLimit(BigDecimal limit) {
        if (limit == null || limit.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Daily limit cannot be negative");
        }
        
        if (limit.compareTo(new BigDecimal("1000000")) > 0) {
            throw new IllegalArgumentException("Daily limit cannot exceed ₹10,00,000");
        }
    }

    private void validateAccountStatus(Account account) {
        if (account.getStatus() != Account.AccountStatus.ACTIVE) {
            throw new InvalidAccountOperationException("Account is not active: " + account.getStatus());
        }
    }

    // Entity-DTO conversion methods
    private Account convertToEntity(AccountDto dto) {
        Account account = new Account(dto.getCustomerId(), dto.getAccountType(), dto.getBranchCode(), dto.getIfscCode());
        return account;
    }

    private AccountDto convertToDto(Account account) {
        AccountDto dto = new AccountDto();
        dto.setAccountNumber(account.getAccountNumber());
        dto.setCustomerId(account.getCustomerId());
        dto.setAccountType(account.getAccountType());
        dto.setBalance(account.getBalance());
        dto.setAvailableBalance(account.getAvailableBalance());
        dto.setStatus(account.getStatus());
        dto.setInterestRate(account.getInterestRate());
        dto.setMinimumBalance(account.getMinimumBalance());
        dto.setDailyTransactionLimit(account.getDailyTransactionLimit());
        dto.setBranchCode(account.getBranchCode());
        dto.setIfscCode(account.getIfscCode());
        dto.setCreatedAt(account.getCreatedAt());
        dto.setLastTransactionDate(account.getLastTransactionDate());
        return dto;
    }
}