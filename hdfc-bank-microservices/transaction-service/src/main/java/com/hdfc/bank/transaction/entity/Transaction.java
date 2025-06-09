package com.hdfc.bank.transaction.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transactions")
public class Transaction {
    
    @Id
    private String transactionId;
    
    @NotBlank(message = "From account is required")
    private String fromAccount;
    
    private String toAccount; // Null for deposits/withdrawals
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be positive")
    @Column(precision = 15, scale = 2)
    private BigDecimal amount;
    
    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;
    
    @Enumerated(EnumType.STRING)
    private TransactionStatus status;
    
    @Size(max = 255, message = "Description cannot exceed 255 characters")
    private String description;
    
    @Size(max = 50, message = "Reference number cannot exceed 50 characters")
    private String referenceNumber;
    
    private LocalDateTime transactionDate;
    private LocalDateTime processedDate;
    
    @DecimalMin(value = "0.0", message = "Charges cannot be negative")
    @Column(precision = 10, scale = 2)
    private BigDecimal charges;
    
    @DecimalMin(value = "0.0", message = "Tax cannot be negative")
    @Column(precision = 10, scale = 2)
    private BigDecimal tax;
    
    @Column(precision = 15, scale = 2)
    private BigDecimal fromAccountBalance; // Balance after transaction
    
    @Column(precision = 15, scale = 2)
    private BigDecimal toAccountBalance; // Balance after transaction (if applicable)
    
    @Size(max = 500, message = "Failure reason cannot exceed 500 characters")
    private String failureReason;
    
    // Banking specific fields
    private String branchCode;
    private String channelUsed; // ATM, NET_BANKING, MOBILE, BRANCH
    private String deviceId;
    private String ipAddress;

    // Default constructor
    public Transaction() {
        this.transactionId = "TXN" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
        this.transactionDate = LocalDateTime.now();
        this.status = TransactionStatus.PENDING;
        this.charges = BigDecimal.ZERO;
        this.tax = BigDecimal.ZERO;
    }

    // Constructor for different transaction types
    public Transaction(String fromAccount, String toAccount, BigDecimal amount, 
                      TransactionType transactionType, String description) {
        this();
        this.fromAccount = fromAccount;
        this.toAccount = toAccount;
        this.amount = amount;
        this.transactionType = transactionType;
        this.description = description;
        calculateCharges();
    }

    // Calculate transaction charges based on type and amount
    private void calculateCharges() {
        switch (this.transactionType) {
            case TRANSFER:
                if (this.amount.compareTo(new BigDecimal("10000")) > 0) {
                    this.charges = new BigDecimal("5.00");
                    this.tax = this.charges.multiply(new BigDecimal("0.18")); // 18% GST
                }
                break;
            case WITHDRAWAL:
                // No charges for first 5 withdrawals per month (simplified)
                if (this.amount.compareTo(new BigDecimal("10000")) > 0) {
                    this.charges = new BigDecimal("21.00"); // HDFC charges
                    this.tax = this.charges.multiply(new BigDecimal("0.18"));
                }
                break;
            case DEPOSIT:
                // Deposits are generally free
                break;
            case PAYMENT:
                this.charges = new BigDecimal("2.50");
                this.tax = this.charges.multiply(new BigDecimal("0.18"));
                break;
        }
    }

    // Getters and Setters
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public String getFromAccount() { return fromAccount; }
    public void setFromAccount(String fromAccount) { this.fromAccount = fromAccount; }

    public String getToAccount() { return toAccount; }
    public void setToAccount(String toAccount) { this.toAccount = toAccount; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public TransactionType getTransactionType() { return transactionType; }
    public void setTransactionType(TransactionType transactionType) { this.transactionType = transactionType; }

    public TransactionStatus getStatus() { return status; }
    public void setStatus(TransactionStatus status) { this.status = status; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getReferenceNumber() { return referenceNumber; }
    public void setReferenceNumber(String referenceNumber) { this.referenceNumber = referenceNumber; }

    public LocalDateTime getTransactionDate() { return transactionDate; }
    public void setTransactionDate(LocalDateTime transactionDate) { this.transactionDate = transactionDate; }

    public LocalDateTime getProcessedDate() { return processedDate; }
    public void setProcessedDate(LocalDateTime processedDate) { this.processedDate = processedDate; }

    public BigDecimal getCharges() { return charges; }
    public void setCharges(BigDecimal charges) { this.charges = charges; }

    public BigDecimal getTax() { return tax; }
    public void setTax(BigDecimal tax) { this.tax = tax; }

    public BigDecimal getFromAccountBalance() { return fromAccountBalance; }
    public void setFromAccountBalance(BigDecimal fromAccountBalance) { this.fromAccountBalance = fromAccountBalance; }

    public BigDecimal getToAccountBalance() { return toAccountBalance; }
    public void setToAccountBalance(BigDecimal toAccountBalance) { this.toAccountBalance = toAccountBalance; }

    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }

    public String getBranchCode() { return branchCode; }
    public void setBranchCode(String branchCode) { this.branchCode = branchCode; }

    public String getChannelUsed() { return channelUsed; }
    public void setChannelUsed(String channelUsed) { this.channelUsed = channelUsed; }

    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    // Business methods
    public BigDecimal getTotalAmount() {
        return this.amount.add(this.charges).add(this.tax);
    }

    public void markAsCompleted() {
        this.status = TransactionStatus.COMPLETED;
        this.processedDate = LocalDateTime.now();
    }

    public void markAsFailed(String reason) {
        this.status = TransactionStatus.FAILED;
        this.failureReason = reason;
        this.processedDate = LocalDateTime.now();
    }

    // Enums
    public enum TransactionType {
        DEPOSIT, WITHDRAWAL, TRANSFER, PAYMENT, REFUND
    }

    public enum TransactionStatus {
        PENDING, PROCESSING, COMPLETED, FAILED, CANCELLED
    }
}