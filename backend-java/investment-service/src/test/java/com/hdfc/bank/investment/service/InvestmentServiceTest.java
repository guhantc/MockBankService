package com.hdfc.bank.investment.service;

import com.hdfc.bank.investment.dto.InvestmentRequest;
import com.hdfc.bank.investment.dto.InvestmentResponse;
import com.hdfc.bank.investment.model.Investment;
import com.hdfc.bank.investment.repository.InvestmentRepository;
import com.hdfc.bank.investment.util.InvestmentMapper;
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
 * Unit tests for InvestmentService
 */
@ExtendWith(MockitoExtension.class)
class InvestmentServiceTest {

    @Mock
    private InvestmentRepository investmentRepository;

    @Mock
    private InvestmentMapper investmentMapper;

    @Mock
    private MarketDataService marketDataService;

    @Mock
    private PortfolioService portfolioService;

    @Mock
    private AccountServiceClient accountServiceClient;

    @InjectMocks
    private InvestmentService investmentService;

    private Investment investment;
    private InvestmentResponse investmentResponse;
    private InvestmentRequest investmentRequest;

    @BeforeEach
    void setUp() {
        investment = new Investment();
        investment.setId(1L);
        investment.setInvestmentId("INV001");
        investment.setUserId("USER001");
        investment.setAccountId("ACC001");
        investment.setInvestmentType(Investment.InvestmentType.MUTUAL_FUND);
        investment.setProductCode("MF001");
        investment.setProductName("HDFC Equity Fund");
        investment.setInvestedAmount(BigDecimal.valueOf(50000.00));
        investment.setCurrentValue(BigDecimal.valueOf(55000.00));
        investment.setUnits(BigDecimal.valueOf(1000.00));
        investment.setNavPrice(BigDecimal.valueOf(55.00));
        investment.setStatus(Investment.InvestmentStatus.ACTIVE);
        investment.setPurchaseDate(LocalDate.now().minusMonths(6));
        investment.setCreatedAt(LocalDateTime.now());

        investmentResponse = new InvestmentResponse();
        investmentResponse.setInvestmentId("INV001");
        investmentResponse.setUserId("USER001");
        investmentResponse.setInvestmentType(Investment.InvestmentType.MUTUAL_FUND);
        investmentResponse.setProductCode("MF001");
        investmentResponse.setProductName("HDFC Equity Fund");
        investmentResponse.setInvestedAmount(BigDecimal.valueOf(50000.00));
        investmentResponse.setCurrentValue(BigDecimal.valueOf(55000.00));
        investmentResponse.setUnits(BigDecimal.valueOf(1000.00));
        investmentResponse.setStatus(Investment.InvestmentStatus.ACTIVE);

        investmentRequest = new InvestmentRequest();
        investmentRequest.setUserId("USER001");
        investmentRequest.setAccountId("ACC001");
        investmentRequest.setInvestmentType(Investment.InvestmentType.MUTUAL_FUND);
        investmentRequest.setProductCode("MF001");
        investmentRequest.setInvestmentAmount(BigDecimal.valueOf(50000.00));
    }

    @Test
    void createInvestment_Success() {
        // Given
        when(accountServiceClient.checkBalance("ACC001")).thenReturn(BigDecimal.valueOf(100000.00));
        when(marketDataService.getCurrentNavPrice("MF001")).thenReturn(BigDecimal.valueOf(50.00));
        when(accountServiceClient.debitAccount("ACC001", BigDecimal.valueOf(50000.00))).thenReturn(true);
        when(investmentRepository.save(any(Investment.class))).thenReturn(investment);
        when(investmentMapper.toResponse(investment)).thenReturn(investmentResponse);

        // When
        InvestmentResponse result = investmentService.createInvestment(investmentRequest);

        // Then
        assertNotNull(result);
        assertEquals("INV001", result.getInvestmentId());
        assertEquals(Investment.InvestmentType.MUTUAL_FUND, result.getInvestmentType());
        verify(accountServiceClient).checkBalance("ACC001");
        verify(marketDataService).getCurrentNavPrice("MF001");
        verify(accountServiceClient).debitAccount("ACC001", BigDecimal.valueOf(50000.00));
        verify(investmentRepository).save(any(Investment.class));
    }

    @Test
    void createInvestment_InsufficientBalance() {
        // Given
        when(accountServiceClient.checkBalance("ACC001")).thenReturn(BigDecimal.valueOf(30000.00)); // Less than investment amount

        // When & Then
        assertThrows(RuntimeException.class, () -> investmentService.createInvestment(investmentRequest));
        verify(accountServiceClient).checkBalance("ACC001");
        verify(marketDataService, never()).getCurrentNavPrice(anyString());
        verify(accountServiceClient, never()).debitAccount(anyString(), any());
        verify(investmentRepository, never()).save(any());
    }

    @Test
    void getInvestmentById_Success() {
        // Given
        when(investmentRepository.findByInvestmentId("INV001")).thenReturn(Optional.of(investment));
        when(investmentMapper.toResponse(investment)).thenReturn(investmentResponse);

        // When
        InvestmentResponse result = investmentService.getInvestmentById("INV001");

        // Then
        assertNotNull(result);
        assertEquals("INV001", result.getInvestmentId());
        verify(investmentRepository).findByInvestmentId("INV001");
        verify(investmentMapper).toResponse(investment);
    }

    @Test
    void getInvestmentsByUserId_Success() {
        // Given
        List<Investment> investments = Arrays.asList(investment);
        when(investmentRepository.findByUserId("USER001")).thenReturn(investments);
        when(investmentMapper.toResponse(investment)).thenReturn(investmentResponse);

        // When
        List<InvestmentResponse> result = investmentService.getInvestmentsByUserId("USER001");

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("INV001", result.get(0).getInvestmentId());
        verify(investmentRepository).findByUserId("USER001");
        verify(investmentMapper).toResponse(investment);
    }

    @Test
    void redeemInvestment_FullRedemption_Success() {
        // Given
        when(investmentRepository.findByInvestmentId("INV001")).thenReturn(Optional.of(investment));
        when(marketDataService.getCurrentNavPrice("MF001")).thenReturn(BigDecimal.valueOf(55.00));
        when(accountServiceClient.creditAccount("ACC001", BigDecimal.valueOf(55000.00))).thenReturn(true);
        when(investmentRepository.save(any(Investment.class))).thenReturn(investment);

        // When
        investmentService.redeemInvestment("INV001", BigDecimal.valueOf(1000.00)); // Full units

        // Then
        verify(investmentRepository).findByInvestmentId("INV001");
        verify(marketDataService).getCurrentNavPrice("MF001");
        verify(accountServiceClient).creditAccount("ACC001", BigDecimal.valueOf(55000.00));
        verify(investmentRepository).save(argThat(inv -> inv.getStatus() == Investment.InvestmentStatus.REDEEMED));
    }

    @Test
    void redeemInvestment_PartialRedemption_Success() {
        // Given
        BigDecimal unitsToRedeem = BigDecimal.valueOf(500.00); // Half units
        BigDecimal expectedRedemptionAmount = BigDecimal.valueOf(27500.00); // 500 * 55
        
        when(investmentRepository.findByInvestmentId("INV001")).thenReturn(Optional.of(investment));
        when(marketDataService.getCurrentNavPrice("MF001")).thenReturn(BigDecimal.valueOf(55.00));
        when(accountServiceClient.creditAccount("ACC001", expectedRedemptionAmount)).thenReturn(true);
        when(investmentRepository.save(any(Investment.class))).thenReturn(investment);

        // When
        investmentService.redeemInvestment("INV001", unitsToRedeem);

        // Then
        verify(investmentRepository).findByInvestmentId("INV001");
        verify(marketDataService).getCurrentNavPrice("MF001");
        verify(accountServiceClient).creditAccount("ACC001", expectedRedemptionAmount);
        verify(investmentRepository).save(argThat(inv -> 
                inv.getUnits().equals(BigDecimal.valueOf(500.00)) &&
                inv.getStatus() == Investment.InvestmentStatus.ACTIVE
        ));
    }

    @Test
    void redeemInvestment_InsufficientUnits() {
        // Given
        BigDecimal unitsToRedeem = BigDecimal.valueOf(1500.00); // More than available
        when(investmentRepository.findByInvestmentId("INV001")).thenReturn(Optional.of(investment));

        // When & Then
        assertThrows(RuntimeException.class, () -> investmentService.redeemInvestment("INV001", unitsToRedeem));
        verify(investmentRepository).findByInvestmentId("INV001");
        verify(marketDataService, never()).getCurrentNavPrice(anyString());
        verify(accountServiceClient, never()).creditAccount(anyString(), any());
        verify(investmentRepository, never()).save(any());
    }

    @Test
    void updateInvestmentValues_Success() {
        // Given
        List<Investment> activeInvestments = Arrays.asList(investment);
        when(investmentRepository.findByStatus(Investment.InvestmentStatus.ACTIVE)).thenReturn(activeInvestments);
        when(marketDataService.getCurrentNavPrice("MF001")).thenReturn(BigDecimal.valueOf(60.00)); // Price increased
        when(investmentRepository.save(any(Investment.class))).thenReturn(investment);

        // When
        investmentService.updateInvestmentValues();

        // Then
        verify(investmentRepository).findByStatus(Investment.InvestmentStatus.ACTIVE);
        verify(marketDataService).getCurrentNavPrice("MF001");
        verify(investmentRepository).save(argThat(inv -> 
                inv.getCurrentValue().equals(BigDecimal.valueOf(60000.00)) && // 1000 * 60
                inv.getNavPrice().equals(BigDecimal.valueOf(60.00))
        ));
    }

    @Test
    void getPortfolioSummary_Success() {
        // Given
        List<Investment> investments = Arrays.asList(investment);
        when(investmentRepository.findByUserId("USER001")).thenReturn(investments);
        when(portfolioService.calculatePortfolioMetrics(investments)).thenReturn(new PortfolioSummary());

        // When
        PortfolioSummary result = investmentService.getPortfolioSummary("USER001");

        // Then
        assertNotNull(result);
        verify(investmentRepository).findByUserId("USER001");
        verify(portfolioService).calculatePortfolioMetrics(investments);
    }

    @Test
    void calculateSIP_Success() {
        // Given
        BigDecimal monthlyAmount = BigDecimal.valueOf(5000.00);
        int tenureMonths = 12;
        BigDecimal expectedReturns = BigDecimal.valueOf(12.0); // 12% annual return

        // When
        SIPCalculation result = investmentService.calculateSIP(monthlyAmount, tenureMonths, expectedReturns);

        // Then
        assertNotNull(result);
        assertTrue(result.getTotalInvestment().equals(BigDecimal.valueOf(60000.00))); // 5000 * 12
        assertTrue(result.getMaturityAmount().compareTo(BigDecimal.valueOf(60000.00)) > 0); // Should be more due to returns
    }

    @Test
    void getInvestmentsByType_Success() {
        // Given
        List<Investment> mutualFunds = Arrays.asList(investment);
        when(investmentRepository.findByUserIdAndInvestmentType("USER001", Investment.InvestmentType.MUTUAL_FUND))
                .thenReturn(mutualFunds);
        when(investmentMapper.toResponse(investment)).thenReturn(investmentResponse);

        // When
        List<InvestmentResponse> result = investmentService.getInvestmentsByType("USER001", Investment.InvestmentType.MUTUAL_FUND);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(investmentRepository).findByUserIdAndInvestmentType("USER001", Investment.InvestmentType.MUTUAL_FUND);
    }

    @Test
    void switchFund_Success() {
        // Given
        String targetProductCode = "MF002";
        when(investmentRepository.findByInvestmentId("INV001")).thenReturn(Optional.of(investment));
        when(marketDataService.getCurrentNavPrice("MF001")).thenReturn(BigDecimal.valueOf(55.00)); // Current fund NAV
        when(marketDataService.getCurrentNavPrice("MF002")).thenReturn(BigDecimal.valueOf(45.00)); // Target fund NAV
        when(investmentRepository.save(any(Investment.class))).thenReturn(investment);

        // When
        investmentService.switchFund("INV001", targetProductCode);

        // Then
        verify(investmentRepository).findByInvestmentId("INV001");
        verify(marketDataService).getCurrentNavPrice("MF001");
        verify(marketDataService).getCurrentNavPrice("MF002");
        verify(investmentRepository).save(argThat(inv -> 
                inv.getProductCode().equals("MF002") &&
                inv.getUnits().compareTo(BigDecimal.valueOf(1000.00)) > 0 // More units due to lower NAV
        ));
    }

    @Test
    void getTopPerformingFunds_Success() {
        // Given
        when(marketDataService.getTopPerformingFunds(Investment.InvestmentType.MUTUAL_FUND, 10))
                .thenReturn(Arrays.asList("MF001", "MF002", "MF003"));

        // When
        List<String> result = investmentService.getTopPerformingFunds(Investment.InvestmentType.MUTUAL_FUND, 10);

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
        verify(marketDataService).getTopPerformingFunds(Investment.InvestmentType.MUTUAL_FUND, 10);
    }

    @Test
    void getInvestmentReturns_Success() {
        // Given
        when(investmentRepository.findByInvestmentId("INV001")).thenReturn(Optional.of(investment));

        // When
        BigDecimal returns = investmentService.getInvestmentReturns("INV001");

        // Then
        assertNotNull(returns);
        assertEquals(BigDecimal.valueOf(5000.00), returns); // 55000 - 50000
        verify(investmentRepository).findByInvestmentId("INV001");
    }
}