package com.hdfc.bank.account.dto;

import com.hdfc.bank.account.entity.Account;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AccountDto {
    
    private String accountNumber;
    
    @NotBlank(message = "Customer ID is required")
    private String customerId;
    
    @NotNull(message = "Account type is required")
    private Account.AccountType accountType;
    
    @DecimalMin(value = "0.0", message = "Balance cannot be negative")
    private BigDecimal balance;
    
    @DecimalMin(value = "0.0", message = "Available balance cannot be negative")
    private BigDecimal availableBalance;
    
    private Account.AccountStatus status;
    
    @DecimalMin(value = "0.0", message = "Interest rate cannot be negative")
    @DecimalMax(value = "100.0", message = "Interest rate cannot exceed 100%")
    private Double interestRate;
    
    @DecimalMin(value = "0.0", message = "Minimum balance cannot be negative")
    private BigDecimal minimumBalance;
    
    @DecimalMin(value = "0.0", message = "Daily transaction limit cannot be negative")
    private BigDecimal dailyTransactionLimit;
    
    @NotBlank(message = "Branch code is required")
    private String branchCode;
    
    @NotBlank(message = "IFSC code is required")
    private String ifscCode;
    
    private LocalDateTime createdAt;
    private LocalDateTime lastTransactionDate;

    // Default constructor
    public AccountDto() {}

    // Constructor with required fields
    public AccountDto(String customerId, Account.AccountType accountType, String branchCode, String ifscCode) {
        this.customerId = customerId;
        this.accountType = accountType;
        this.branchCode = branchCode;
        this.ifscCode = ifscCode;
    }

    // Getters and Setters
    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public Account.AccountType getAccountType() { return accountType; }
    public void setAccountType(Account.AccountType accountType) { this.accountType = accountType; }

    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }

    public BigDecimal getAvailableBalance() { return availableBalance; }
    public void setAvailableBalance(BigDecimal availableBalance) { this.availableBalance = availableBalance; }

    public Account.AccountStatus getStatus() { return status; }
    public void setStatus(Account.AccountStatus status) { this.status = status; }

    public Double getInterestRate() { return interestRate; }
    public void setInterestRate(Double interestRate) { this.interestRate = interestRate; }

    public BigDecimal getMinimumBalance() { return minimumBalance; }
    public void setMinimumBalance(BigDecimal minimumBalance) { this.minimumBalance = minimumBalance; }

    public BigDecimal getDailyTransactionLimit() { return dailyTransactionLimit; }
    public void setDailyTransactionLimit(BigDecimal dailyTransactionLimit) { this.dailyTransactionLimit = dailyTransactionLimit; }

    public String getBranchCode() { return branchCode; }
    public void setBranchCode(String branchCode) { this.branchCode = branchCode; }

    public String getIfscCode() { return ifscCode; }
    public void setIfscCode(String ifscCode) { this.ifscCode = ifscCode; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getLastTransactionDate() { return lastTransactionDate; }
    public void setLastTransactionDate(LocalDateTime lastTransactionDate) { this.lastTransactionDate = lastTransactionDate; }
}