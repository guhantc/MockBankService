package com.hdfc.bank.account.integration;

import com.hdfc.bank.account.dto.AccountCreateRequest;
import com.hdfc.bank.account.model.Account;
import com.hdfc.bank.account.repository.AccountRepository;
import com.hdfc.bank.test.util.IntegrationTestBase;
import com.hdfc.bank.test.util.TestDataBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for Account Service
 */
class AccountServiceIntegrationTest extends IntegrationTestBase {

    @Autowired
    private AccountRepository accountRepository;

    @Test
    void createAccount_Success() throws Exception {
        // Given
        AccountCreateRequest request = new AccountCreateRequest();
        request.setUserId("USER001");
        request.setAccountType(Account.AccountType.SAVINGS);
        request.setInitialDeposit(BigDecimal.valueOf(5000.00));

        // When & Then
        mockMvc.perform(post("/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("USER001"))
                .andExpect(jsonPath("$.accountType").value("SAVINGS"))
                .andExpect(jsonPath("$.balance").value(5000.00));
    }

    @Test
    void getAccountById_Success() throws Exception {
        // Given
        Account account = TestDataBuilder.createAccount()
                .withAccountId("ACC123")
                .withUserId("USER001")
                .withBalance(BigDecimal.valueOf(10000.00))
                .build();
        accountRepository.save(account);

        // When & Then
        mockMvc.perform(get("/accounts/ACC123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value("ACC123"))
                .andExpect(jsonPath("$.userId").value("USER001"))
                .andExpect(jsonPath("$.balance").value(10000.00));
    }

    @Test
    void transferFunds_Success() throws Exception {
        // Given
        Account sourceAccount = TestDataBuilder.createAccount()
                .withAccountId("ACC001")
                .withBalance(BigDecimal.valueOf(10000.00))
                .build();
        Account targetAccount = TestDataBuilder.createAccount()
                .withAccountId("ACC002")
                .withBalance(BigDecimal.valueOf(5000.00))
                .build();
        
        accountRepository.save(sourceAccount);
        accountRepository.save(targetAccount);

        String transferRequest = """
            {
                "sourceAccountId": "ACC001",
                "targetAccountId": "ACC002",
                "amount": 2000.00,
                "description": "Test transfer"
            }
            """;

        // When & Then
        mockMvc.perform(post("/accounts/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(transferRequest))
                .andExpect(status().isOk());

        // Verify balances
        mockMvc.perform(get("/accounts/ACC001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(8000.00));

        mockMvc.perform(get("/accounts/ACC002"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(7000.00));
    }

    @Test
    void freezeAccount_Success() throws Exception {
        // Given
        Account account = TestDataBuilder.createAccount()
                .withAccountId("ACC123")
                .withStatus(Account.AccountStatus.ACTIVE)
                .build();
        accountRepository.save(account);

        // When & Then
        mockMvc.perform(put("/accounts/ACC123/freeze"))
                .andExpect(status().isOk());

        // Verify status
        mockMvc.perform(get("/accounts/ACC123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FROZEN"));
    }

    @Test
    void getAccountsByUserId_Success() throws Exception {
        // Given
        Account account1 = TestDataBuilder.createAccount()
                .withAccountId("ACC001")
                .withUserId("USER001")
                .withAccountType(Account.AccountType.SAVINGS)
                .build();
        Account account2 = TestDataBuilder.createAccount()
                .withAccountId("ACC002")
                .withUserId("USER001")
                .withAccountType(Account.AccountType.CURRENT)
                .build();
        
        accountRepository.save(account1);
        accountRepository.save(account2);

        // When & Then
        mockMvc.perform(get("/accounts/user/USER001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].userId").value("USER001"))
                .andExpect(jsonPath("$[1].userId").value("USER001"));
    }

    @Test
    void updateBalance_Credit_Success() throws Exception {
        // Given
        Account account = TestDataBuilder.createAccount()
                .withAccountId("ACC123")
                .withBalance(BigDecimal.valueOf(5000.00))
                .build();
        accountRepository.save(account);

        String balanceUpdateRequest = """
            {
                "amount": 2000.00,
                "transactionType": "CREDIT",
                "description": "Credit deposit"
            }
            """;

        // When & Then
        mockMvc.perform(put("/accounts/ACC123/balance")
                .contentType(MediaType.APPLICATION_JSON)
                .content(balanceUpdateRequest))
                .andExpect(status().isOk());

        // Verify balance
        mockMvc.perform(get("/accounts/ACC123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(7000.00));
    }

    @Test
    void updateBalance_Debit_InsufficientFunds() throws Exception {
        // Given
        Account account = TestDataBuilder.createAccount()
                .withAccountId("ACC123")
                .withBalance(BigDecimal.valueOf(1000.00))
                .build();
        accountRepository.save(account);

        String balanceUpdateRequest = """
            {
                "amount": 2000.00,
                "transactionType": "DEBIT",
                "description": "Debit withdrawal"
            }
            """;

        // When & Then
        mockMvc.perform(put("/accounts/ACC123/balance")
                .contentType(MediaType.APPLICATION_JSON)
                .content(balanceUpdateRequest))
                .andExpect(status().isBadRequest());

        // Verify balance unchanged
        mockMvc.perform(get("/accounts/ACC123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(1000.00));
    }

    @Test
    void completeAccountLifecycle_Success() throws Exception {
        // Step 1: Create account
        AccountCreateRequest createRequest = new AccountCreateRequest();
        createRequest.setUserId("USER001");
        createRequest.setAccountType(Account.AccountType.SAVINGS);
        createRequest.setInitialDeposit(BigDecimal.valueOf(5000.00));

        String createResponse = mockMvc.perform(post("/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(createRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String accountId = fromJson(createResponse, Account.class).getAccountId();

        // Step 2: Credit money
        String creditRequest = """
            {
                "amount": 3000.00,
                "transactionType": "CREDIT",
                "description": "Salary credit"
            }
            """;

        mockMvc.perform(put("/accounts/" + accountId + "/balance")
                .contentType(MediaType.APPLICATION_JSON)
                .content(creditRequest))
                .andExpect(status().isOk());

        // Step 3: Check balance
        mockMvc.perform(get("/accounts/" + accountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(8000.00));

        // Step 4: Freeze account
        mockMvc.perform(put("/accounts/" + accountId + "/freeze"))
                .andExpect(status().isOk());

        // Step 5: Verify frozen status
        mockMvc.perform(get("/accounts/" + accountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FROZEN"));

        // Step 6: Activate account
        mockMvc.perform(put("/accounts/" + accountId + "/activate"))
                .andExpect(status().isOk());

        // Step 7: Verify active status
        mockMvc.perform(get("/accounts/" + accountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }
}