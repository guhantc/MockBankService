package com.hdfc.bank.account.repository;

import com.hdfc.bank.account.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, String> {
    
    List<Account> findByCustomerId(String customerId);
    
    List<Account> findByAccountType(Account.AccountType accountType);
    
    List<Account> findByStatus(Account.AccountStatus status);
    
    List<Account> findByCustomerIdAndAccountType(String customerId, Account.AccountType accountType);
    
    List<Account> findByCustomerIdAndStatus(String customerId, Account.AccountStatus status);
    
    Optional<Account> findByAccountNumber(String accountNumber);
    
    @Query("SELECT a FROM Account a WHERE a.balance >= :minBalance AND a.balance <= :maxBalance")
    List<Account> findByBalanceRange(@Param("minBalance") BigDecimal minBalance, 
                                   @Param("maxBalance") BigDecimal maxBalance);
    
    @Query("SELECT a FROM Account a WHERE a.branchCode = :branchCode")
    List<Account> findByBranchCode(@Param("branchCode") String branchCode);
    
    @Query("SELECT a FROM Account a WHERE a.customerId = :customerId AND a.accountType = :accountType AND a.status = 'ACTIVE'")
    Optional<Account> findActiveAccountByCustomerAndType(@Param("customerId") String customerId, 
                                                        @Param("accountType") Account.AccountType accountType);
    
    @Query("SELECT COUNT(a) FROM Account a WHERE a.customerId = :customerId AND a.status = 'ACTIVE'")
    int countActiveAccountsByCustomer(@Param("customerId") String customerId);
    
    @Query("SELECT SUM(a.balance) FROM Account a WHERE a.customerId = :customerId AND a.status = 'ACTIVE'")
    BigDecimal getTotalBalanceByCustomer(@Param("customerId") String customerId);
    
    boolean existsByCustomerIdAndAccountType(String customerId, Account.AccountType accountType);
}