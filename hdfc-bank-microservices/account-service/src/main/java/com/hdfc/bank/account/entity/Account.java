package com.hdfc.bank.account.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "accounts")
public class Account {
    
    @Id
    private String accountNumber;
    
    @NotBlank(message = "Customer ID is required")
    private String customerId;
    
    @Enumerated(EnumType.STRING)
    private AccountType accountType;
    
    @DecimalMin(value = "0.0", message = "Balance cannot be negative")
    @Column(precision = 15, scale = 2)
    private BigDecimal balance;
    
    @DecimalMin(value = "0.0", message = "Available balance cannot be negative")
    @Column(precision = 15, scale = 2)
    private BigDecimal availableBalance;
    
    @Enumerated(EnumType.STRING)
    private AccountStatus status;
    
    @DecimalMin(value = "0.0", message = "Interest rate cannot be negative")
    @DecimalMax(value = "100.0", message = "Interest rate cannot exceed 100%")
    private Double interestRate;
    
    @DecimalMin(value = "0.0", message = "Minimum balance cannot be negative")
    @Column(precision = 15, scale = 2)
    private BigDecimal minimumBalance;
    
    @DecimalMin(value = "0.0", message = "Daily transaction limit cannot be negative")
    @Column(precision = 15, scale = 2)
    private BigDecimal dailyTransactionLimit;
    
    @NotBlank(message = "Branch code is required")
    private String branchCode;
    
    @NotBlank(message = "IFSC code is required")
    private String ifscCode;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastTransactionDate;

    // Default constructor
    public Account() {
        this.accountNumber = generateAccountNumber();
        this.balance = BigDecimal.ZERO;
        this.availableBalance = BigDecimal.ZERO;
        this.status = AccountStatus.ACTIVE;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Constructor with customer ID and account type
    public Account(String customerId, AccountType accountType, String branchCode, String ifscCode) {
        this();
        this.customerId = customerId;
        this.accountType = accountType;
        this.branchCode = branchCode;
        this.ifscCode = ifscCode;
        setDefaultValues(accountType);
    }

    private String generateAccountNumber() {
        // HDFC format: 50100 + 10 digit number
        return "50100" + String.format("%010d", Math.abs(UUID.randomUUID().hashCode()));
    }

    private void setDefaultValues(AccountType accountType) {
        switch (accountType) {
            case SAVINGS:
                this.interestRate = 3.5;
                this.minimumBalance = new BigDecimal("10000");
                this.dailyTransactionLimit = new BigDecimal("50000");
                break;
            case CURRENT:
                this.interestRate = 0.0;
                this.minimumBalance = new BigDecimal("25000");
                this.dailyTransactionLimit = new BigDecimal("200000");
                break;
            case SALARY:
                this.interestRate = 3.5;
                this.minimumBalance = BigDecimal.ZERO;
                this.dailyTransactionLimit = new BigDecimal("100000");
                break;
            case FIXED_DEPOSIT:
                this.interestRate = 6.5;
                this.minimumBalance = new BigDecimal("100000");
                this.dailyTransactionLimit = BigDecimal.ZERO;
                break;
            case RECURRING_DEPOSIT:
                this.interestRate = 6.0;
                this.minimumBalance = new BigDecimal("500");
                this.dailyTransactionLimit = BigDecimal.ZERO;
                break;
        }
        this.availableBalance = this.balance;
    }

    // Getters and Setters
    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public AccountType getAccountType() { return accountType; }
    public void setAccountType(AccountType accountType) { this.accountType = accountType; }

    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }

    public BigDecimal getAvailableBalance() { return availableBalance; }
    public void setAvailableBalance(BigDecimal availableBalance) { this.availableBalance = availableBalance; }

    public AccountStatus getStatus() { return status; }
    public void setStatus(AccountStatus status) { this.status = status; }

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

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public LocalDateTime getLastTransactionDate() { return lastTransactionDate; }
    public void setLastTransactionDate(LocalDateTime lastTransactionDate) { this.lastTransactionDate = lastTransactionDate; }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Business methods
    public boolean canDebit(BigDecimal amount) {
        BigDecimal afterDebitBalance = this.balance.subtract(amount);
        return afterDebitBalance.compareTo(this.minimumBalance) >= 0 && 
               amount.compareTo(this.dailyTransactionLimit) <= 0;
    }

    public void debit(BigDecimal amount) {
        if (canDebit(amount)) {
            this.balance = this.balance.subtract(amount);
            this.availableBalance = this.balance;
            this.lastTransactionDate = LocalDateTime.now();
        } else {
            throw new IllegalArgumentException("Insufficient balance or limit exceeded");
        }
    }

    public void credit(BigDecimal amount) {
        this.balance = this.balance.add(amount);
        this.availableBalance = this.balance;
        this.lastTransactionDate = LocalDateTime.now();
    }

    // Enums
    public enum AccountType {
        SAVINGS, CURRENT, SALARY, FIXED_DEPOSIT, RECURRING_DEPOSIT
    }

    public enum AccountStatus {
        ACTIVE, INACTIVE, SUSPENDED, CLOSED, FROZEN
    }
}