package com.hdfc.bank.loan.service;

import com.hdfc.bank.loan.dto.LoanApplicationRequest;
import com.hdfc.bank.loan.dto.LoanResponse;
import com.hdfc.bank.loan.model.Loan;
import com.hdfc.bank.loan.repository.LoanRepository;
import com.hdfc.bank.loan.util.LoanMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for LoanService
 */
@ExtendWith(MockitoExtension.class)
class LoanServiceTest {

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private LoanMapper loanMapper;

    @Mock
    private CreditCheckService creditCheckService;

    @Mock
    private LoanEligibilityService eligibilityService;

    @InjectMocks
    private LoanService loanService;

    private Loan loan;
    private LoanResponse loanResponse;
    private LoanApplicationRequest applicationRequest;

    @BeforeEach
    void setUp() {
        loan = new Loan();
        loan.setId(1L);
        loan.setLoanId("LOAN001");
        loan.setUserId("USER001");
        loan.setAccountId("ACC001");
        loan.setLoanType(Loan.LoanType.HOME_LOAN);
        loan.setPrincipalAmount(BigDecimal.valueOf(500000.00));
        loan.setInterestRate(BigDecimal.valueOf(8.5));
        loan.setTenureMonths(240);
        loan.setEmiAmount(BigDecimal.valueOf(4386.24));
        loan.setStatus(Loan.LoanStatus.APPROVED);
        loan.setApplicationDate(LocalDate.now());
        loan.setCreatedAt(LocalDateTime.now());

        loanResponse = new LoanResponse();
        loanResponse.setLoanId("LOAN001");
        loanResponse.setUserId("USER001");
        loanResponse.setLoanType(Loan.LoanType.HOME_LOAN);
        loanResponse.setPrincipalAmount(BigDecimal.valueOf(500000.00));
        loanResponse.setInterestRate(BigDecimal.valueOf(8.5));
        loanResponse.setTenureMonths(240);
        loanResponse.setEmiAmount(BigDecimal.valueOf(4386.24));
        loanResponse.setStatus(Loan.LoanStatus.APPROVED);

        applicationRequest = new LoanApplicationRequest();
        applicationRequest.setUserId("USER001");
        applicationRequest.setAccountId("ACC001");
        applicationRequest.setLoanType(Loan.LoanType.HOME_LOAN);
        applicationRequest.setPrincipalAmount(BigDecimal.valueOf(500000.00));
        applicationRequest.setTenureMonths(240);
        applicationRequest.setAnnualIncome(BigDecimal.valueOf(1200000.00));
        applicationRequest.setPurpose("Home Purchase");
    }

    @Test
    void applyForLoan_Success() {
        // Given
        when(eligibilityService.checkEligibility(applicationRequest)).thenReturn(true);
        when(creditCheckService.performCreditCheck("USER001")).thenReturn(750); // Good credit score
        when(loanRepository.save(any(Loan.class))).thenReturn(loan);
        when(loanMapper.toResponse(loan)).thenReturn(loanResponse);

        // When
        LoanResponse result = loanService.applyForLoan(applicationRequest);

        // Then
        assertNotNull(result);
        assertEquals("LOAN001", result.getLoanId());
        assertEquals(Loan.LoanType.HOME_LOAN, result.getLoanType());
        verify(eligibilityService).checkEligibility(applicationRequest);
        verify(creditCheckService).performCreditCheck("USER001");
        verify(loanRepository).save(any(Loan.class));
    }

    @Test
    void applyForLoan_NotEligible() {
        // Given
        when(eligibilityService.checkEligibility(applicationRequest)).thenReturn(false);

        // When & Then
        assertThrows(RuntimeException.class, () -> loanService.applyForLoan(applicationRequest));
        verify(eligibilityService).checkEligibility(applicationRequest);
        verify(creditCheckService, never()).performCreditCheck(anyString());
        verify(loanRepository, never()).save(any());
    }

    @Test
    void applyForLoan_LowCreditScore() {
        // Given
        when(eligibilityService.checkEligibility(applicationRequest)).thenReturn(true);
        when(creditCheckService.performCreditCheck("USER001")).thenReturn(600); // Low credit score

        // When & Then
        assertThrows(RuntimeException.class, () -> loanService.applyForLoan(applicationRequest));
        verify(eligibilityService).checkEligibility(applicationRequest);
        verify(creditCheckService).performCreditCheck("USER001");
        verify(loanRepository, never()).save(any());
    }

    @Test
    void getLoanById_Success() {
        // Given
        when(loanRepository.findByLoanId("LOAN001")).thenReturn(Optional.of(loan));
        when(loanMapper.toResponse(loan)).thenReturn(loanResponse);

        // When
        LoanResponse result = loanService.getLoanById("LOAN001");

        // Then
        assertNotNull(result);
        assertEquals("LOAN001", result.getLoanId());
        verify(loanRepository).findByLoanId("LOAN001");
        verify(loanMapper).toResponse(loan);
    }

    @Test
    void getLoansByUserId_Success() {
        // Given
        List<Loan> loans = Arrays.asList(loan);
        when(loanRepository.findByUserId("USER001")).thenReturn(loans);
        when(loanMapper.toResponse(loan)).thenReturn(loanResponse);

        // When
        List<LoanResponse> result = loanService.getLoansByUserId("USER001");

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("LOAN001", result.get(0).getLoanId());
        verify(loanRepository).findByUserId("USER001");
        verify(loanMapper).toResponse(loan);
    }

    @Test
    void approveLoan_Success() {
        // Given
        loan.setStatus(Loan.LoanStatus.PENDING);
        when(loanRepository.findByLoanId("LOAN001")).thenReturn(Optional.of(loan));
        when(loanRepository.save(any(Loan.class))).thenReturn(loan);

        // When
        loanService.approveLoan("LOAN001", "MANAGER001");

        // Then
        verify(loanRepository).findByLoanId("LOAN001");
        verify(loanRepository).save(argThat(l -> l.getStatus() == Loan.LoanStatus.APPROVED));
    }

    @Test
    void rejectLoan_Success() {
        // Given
        loan.setStatus(Loan.LoanStatus.PENDING);
        when(loanRepository.findByLoanId("LOAN001")).thenReturn(Optional.of(loan));
        when(loanRepository.save(any(Loan.class))).thenReturn(loan);

        // When
        loanService.rejectLoan("LOAN001", "MANAGER001", "Insufficient income");

        // Then
        verify(loanRepository).findByLoanId("LOAN001");
        verify(loanRepository).save(argThat(l -> l.getStatus() == Loan.LoanStatus.REJECTED));
    }

    @Test
    void disburseLoan_Success() {
        // Given
        when(loanRepository.findByLoanId("LOAN001")).thenReturn(Optional.of(loan));
        when(loanRepository.save(any(Loan.class))).thenReturn(loan);

        // When
        loanService.disburseLoan("LOAN001");

        // Then
        verify(loanRepository).findByLoanId("LOAN001");
        verify(loanRepository).save(argThat(l -> l.getStatus() == Loan.LoanStatus.DISBURSED));
    }

    @Test
    void calculateEMI_Success() {
        // Given
        BigDecimal principal = BigDecimal.valueOf(500000.00);
        BigDecimal interestRate = BigDecimal.valueOf(8.5);
        int tenureMonths = 240;

        // When
        BigDecimal emi = loanService.calculateEMI(principal, interestRate, tenureMonths);

        // Then
        assertNotNull(emi);
        assertTrue(emi.compareTo(BigDecimal.ZERO) > 0);
        // Approximate EMI calculation verification
        assertTrue(emi.compareTo(BigDecimal.valueOf(4000)) > 0);
        assertTrue(emi.compareTo(BigDecimal.valueOf(5000)) < 0);
    }

    @Test
    void getOutstandingAmount_Success() {
        // Given
        when(loanRepository.findByLoanId("LOAN001")).thenReturn(Optional.of(loan));

        // When
        BigDecimal outstanding = loanService.getOutstandingAmount("LOAN001");

        // Then
        assertNotNull(outstanding);
        verify(loanRepository).findByLoanId("LOAN001");
    }

    @Test
    void getLoansByStatus_Success() {
        // Given
        List<Loan> pendingLoans = Arrays.asList(loan);
        when(loanRepository.findByStatus(Loan.LoanStatus.PENDING)).thenReturn(pendingLoans);
        when(loanMapper.toResponse(loan)).thenReturn(loanResponse);

        // When
        List<LoanResponse> result = loanService.getLoansByStatus(Loan.LoanStatus.PENDING);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(loanRepository).findByStatus(Loan.LoanStatus.PENDING);
    }

    @Test
    void prepayLoan_Success() {
        // Given
        BigDecimal prepaymentAmount = BigDecimal.valueOf(100000.00);
        when(loanRepository.findByLoanId("LOAN001")).thenReturn(Optional.of(loan));
        when(loanRepository.save(any(Loan.class))).thenReturn(loan);

        // When
        loanService.prepayLoan("LOAN001", prepaymentAmount);

        // Then
        verify(loanRepository).findByLoanId("LOAN001");
        verify(loanRepository).save(any(Loan.class));
    }

    @Test
    void closeLoan_Success() {
        // Given
        loan.setStatus(Loan.LoanStatus.DISBURSED);
        when(loanRepository.findByLoanId("LOAN001")).thenReturn(Optional.of(loan));
        when(loanRepository.save(any(Loan.class))).thenReturn(loan);

        // When
        loanService.closeLoan("LOAN001");

        // Then
        verify(loanRepository).findByLoanId("LOAN001");
        verify(loanRepository).save(argThat(l -> l.getStatus() == Loan.LoanStatus.CLOSED));
    }
}