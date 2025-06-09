package com.hdfc.bank.account.controller;

import com.hdfc.bank.account.dto.AccountDto;
import com.hdfc.bank.account.entity.Account;
import com.hdfc.bank.account.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/accounts")
@CrossOrigin(origins = "*")
public class AccountController {

    @Autowired
    private AccountService accountService;

    @PostMapping
    public ResponseEntity<AccountDto> createAccount(@Valid @RequestBody AccountDto accountDto) {
        AccountDto createdAccount = accountService.createAccount(accountDto);
        return new ResponseEntity<>(createdAccount, HttpStatus.CREATED);
    }

    @GetMapping("/{accountNumber}")
    public ResponseEntity<AccountDto> getAccountByNumber(@PathVariable String accountNumber) {
        AccountDto account = accountService.getAccountByNumber(accountNumber);
        return ResponseEntity.ok(account);
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<AccountDto>> getAccountsByCustomerId(@PathVariable String customerId) {
        List<AccountDto> accounts = accountService.getAccountsByCustomerId(customerId);
        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/type/{accountType}")
    public ResponseEntity<List<AccountDto>> getAccountsByType(@PathVariable Account.AccountType accountType) {
        List<AccountDto> accounts = accountService.getAccountsByType(accountType);
        return ResponseEntity.ok(accounts);
    }

    @GetMapping
    public ResponseEntity<List<AccountDto>> getAllAccounts() {
        List<AccountDto> accounts = accountService.getAllAccounts();
        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/{accountNumber}/balance")
    public ResponseEntity<Map<String, BigDecimal>> getBalance(@PathVariable String accountNumber) {
        BigDecimal balance = accountService.getBalance(accountNumber);
        return ResponseEntity.ok(Map.of("balance", balance));
    }

    @PostMapping("/{accountNumber}/deposit")
    public ResponseEntity<AccountDto> depositMoney(@PathVariable String accountNumber, 
                                                  @RequestBody Map<String, BigDecimal> request) {
        BigDecimal amount = request.get("amount");
        AccountDto updatedAccount = accountService.depositMoney(accountNumber, amount);
        return ResponseEntity.ok(updatedAccount);
    }

    @PostMapping("/{accountNumber}/withdraw")
    public ResponseEntity<AccountDto> withdrawMoney(@PathVariable String accountNumber, 
                                                   @RequestBody Map<String, BigDecimal> request) {
        BigDecimal amount = request.get("amount");
        AccountDto updatedAccount = accountService.withdrawMoney(accountNumber, amount);
        return ResponseEntity.ok(updatedAccount);
    }

    @PostMapping("/transfer")
    public ResponseEntity<String> transferMoney(@RequestBody Map<String, Object> transferRequest) {
        String fromAccount = (String) transferRequest.get("fromAccount");
        String toAccount = (String) transferRequest.get("toAccount");
        BigDecimal amount = new BigDecimal(transferRequest.get("amount").toString());
        
        accountService.transferMoney(fromAccount, toAccount, amount);
        return ResponseEntity.ok("Transfer completed successfully");
    }

    @PatchMapping("/{accountNumber}/status")
    public ResponseEntity<AccountDto> updateAccountStatus(@PathVariable String accountNumber, 
                                                         @RequestBody Account.AccountStatus status) {
        AccountDto updatedAccount = accountService.updateAccountStatus(accountNumber, status);
        return ResponseEntity.ok(updatedAccount);
    }

    @PatchMapping("/{accountNumber}/daily-limit")
    public ResponseEntity<AccountDto> updateDailyLimit(@PathVariable String accountNumber, 
                                                      @RequestBody Map<String, BigDecimal> request) {
        BigDecimal newLimit = request.get("limit");
        AccountDto updatedAccount = accountService.updateDailyLimit(accountNumber, newLimit);
        return ResponseEntity.ok(updatedAccount);
    }

    @GetMapping("/customer/{customerId}/total-balance")
    public ResponseEntity<Map<String, BigDecimal>> getTotalBalance(@PathVariable String customerId) {
        BigDecimal totalBalance = accountService.getTotalBalanceByCustomer(customerId);
        return ResponseEntity.ok(Map.of("totalBalance", totalBalance));
    }

    @GetMapping("/customer/{customerId}/account-count")
    public ResponseEntity<Map<String, Integer>> getActiveAccountCount(@PathVariable String customerId) {
        int count = accountService.getActiveAccountCount(customerId);
        return ResponseEntity.ok(Map.of("activeAccountCount", count));
    }

    @PostMapping("/{accountNumber}/close")
    public ResponseEntity<AccountDto> closeAccount(@PathVariable String accountNumber) {
        AccountDto closedAccount = accountService.closeAccount(accountNumber);
        return ResponseEntity.ok(closedAccount);
    }

    // Health check endpoint
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Account Service is running");
    }
}