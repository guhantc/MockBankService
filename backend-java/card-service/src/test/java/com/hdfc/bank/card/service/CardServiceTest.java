package com.hdfc.bank.card.service;

import com.hdfc.bank.card.dto.CardApplicationRequest;
import com.hdfc.bank.card.dto.CardResponse;
import com.hdfc.bank.card.model.Card;
import com.hdfc.bank.card.repository.CardRepository;
import com.hdfc.bank.card.util.CardMapper;
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
 * Unit tests for CardService
 */
@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private CardMapper cardMapper;

    @Mock
    private CardEligibilityService eligibilityService;

    @Mock
    private CardNumberGenerator cardNumberGenerator;

    @InjectMocks
    private CardService cardService;

    private Card card;
    private CardResponse cardResponse;
    private CardApplicationRequest applicationRequest;

    @BeforeEach
    void setUp() {
        card = new Card();
        card.setId(1L);
        card.setCardId("CARD001");
        card.setUserId("USER001");
        card.setAccountId("ACC001");
        card.setCardNumber("4567-1234-5678-9012");
        card.setCardType(Card.CardType.CREDIT_CARD);
        card.setCardCategory(Card.CardCategory.PLATINUM);
        card.setCreditLimit(BigDecimal.valueOf(100000.00));
        card.setAvailableLimit(BigDecimal.valueOf(100000.00));
        card.setExpiryDate(LocalDate.now().plusYears(5));
        card.setStatus(Card.CardStatus.ACTIVE);
        card.setCreatedAt(LocalDateTime.now());

        cardResponse = new CardResponse();
        cardResponse.setCardId("CARD001");
        cardResponse.setUserId("USER001");
        cardResponse.setCardNumber("4567-****-****-9012"); // Masked
        cardResponse.setCardType(Card.CardType.CREDIT_CARD);
        cardResponse.setCardCategory(Card.CardCategory.PLATINUM);
        cardResponse.setCreditLimit(BigDecimal.valueOf(100000.00));
        cardResponse.setAvailableLimit(BigDecimal.valueOf(100000.00));
        cardResponse.setStatus(Card.CardStatus.ACTIVE);

        applicationRequest = new CardApplicationRequest();
        applicationRequest.setUserId("USER001");
        applicationRequest.setAccountId("ACC001");
        applicationRequest.setCardType(Card.CardType.CREDIT_CARD);
        applicationRequest.setCardCategory(Card.CardCategory.PLATINUM);
        applicationRequest.setRequestedLimit(BigDecimal.valueOf(100000.00));
        applicationRequest.setAnnualIncome(BigDecimal.valueOf(1200000.00));
    }

    @Test
    void applyForCard_Success() {
        // Given
        when(eligibilityService.checkEligibility(applicationRequest)).thenReturn(true);
        when(cardNumberGenerator.generateCardNumber()).thenReturn("4567123456789012");
        when(cardRepository.save(any(Card.class))).thenReturn(card);
        when(cardMapper.toResponse(card)).thenReturn(cardResponse);

        // When
        CardResponse result = cardService.applyForCard(applicationRequest);

        // Then
        assertNotNull(result);
        assertEquals("CARD001", result.getCardId());
        assertEquals(Card.CardType.CREDIT_CARD, result.getCardType());
        verify(eligibilityService).checkEligibility(applicationRequest);
        verify(cardNumberGenerator).generateCardNumber();
        verify(cardRepository).save(any(Card.class));
    }

    @Test
    void applyForCard_NotEligible() {
        // Given
        when(eligibilityService.checkEligibility(applicationRequest)).thenReturn(false);

        // When & Then
        assertThrows(RuntimeException.class, () -> cardService.applyForCard(applicationRequest));
        verify(eligibilityService).checkEligibility(applicationRequest);
        verify(cardNumberGenerator, never()).generateCardNumber();
        verify(cardRepository, never()).save(any());
    }

    @Test
    void getCardById_Success() {
        // Given
        when(cardRepository.findByCardId("CARD001")).thenReturn(Optional.of(card));
        when(cardMapper.toResponse(card)).thenReturn(cardResponse);

        // When
        CardResponse result = cardService.getCardById("CARD001");

        // Then
        assertNotNull(result);
        assertEquals("CARD001", result.getCardId());
        verify(cardRepository).findByCardId("CARD001");
        verify(cardMapper).toResponse(card);
    }

    @Test
    void getCardsByUserId_Success() {
        // Given
        List<Card> cards = Arrays.asList(card);
        when(cardRepository.findByUserId("USER001")).thenReturn(cards);
        when(cardMapper.toResponse(card)).thenReturn(cardResponse);

        // When
        List<CardResponse> result = cardService.getCardsByUserId("USER001");

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("CARD001", result.get(0).getCardId());
        verify(cardRepository).findByUserId("USER001");
        verify(cardMapper).toResponse(card);
    }

    @Test
    void activateCard_Success() {
        // Given
        card.setStatus(Card.CardStatus.INACTIVE);
        when(cardRepository.findByCardId("CARD001")).thenReturn(Optional.of(card));
        when(cardRepository.save(any(Card.class))).thenReturn(card);

        // When
        cardService.activateCard("CARD001", "1234");

        // Then
        verify(cardRepository).findByCardId("CARD001");
        verify(cardRepository).save(argThat(c -> c.getStatus() == Card.CardStatus.ACTIVE));
    }

    @Test
    void blockCard_Success() {
        // Given
        when(cardRepository.findByCardId("CARD001")).thenReturn(Optional.of(card));
        when(cardRepository.save(any(Card.class))).thenReturn(card);

        // When
        cardService.blockCard("CARD001", "Lost card");

        // Then
        verify(cardRepository).findByCardId("CARD001");
        verify(cardRepository).save(argThat(c -> c.getStatus() == Card.CardStatus.BLOCKED));
    }

    @Test
    void processTransaction_Credit_Success() {
        // Given
        BigDecimal transactionAmount = BigDecimal.valueOf(5000.00);
        BigDecimal expectedAvailableLimit = BigDecimal.valueOf(95000.00);
        when(cardRepository.findByCardId("CARD001")).thenReturn(Optional.of(card));
        when(cardRepository.save(any(Card.class))).thenReturn(card);

        // When
        cardService.processTransaction("CARD001", transactionAmount, Card.TransactionType.PURCHASE);

        // Then
        verify(cardRepository).findByCardId("CARD001");
        verify(cardRepository).save(argThat(c -> c.getAvailableLimit().equals(expectedAvailableLimit)));
    }

    @Test
    void processTransaction_InsufficientLimit() {
        // Given
        BigDecimal transactionAmount = BigDecimal.valueOf(150000.00); // More than available limit
        when(cardRepository.findByCardId("CARD001")).thenReturn(Optional.of(card));

        // When & Then
        assertThrows(RuntimeException.class, 
            () -> cardService.processTransaction("CARD001", transactionAmount, Card.TransactionType.PURCHASE));
        verify(cardRepository).findByCardId("CARD001");
        verify(cardRepository, never()).save(any());
    }

    @Test
    void processPayment_Success() {
        // Given
        card.setAvailableLimit(BigDecimal.valueOf(80000.00)); // Some amount already used
        BigDecimal paymentAmount = BigDecimal.valueOf(10000.00);
        BigDecimal expectedAvailableLimit = BigDecimal.valueOf(90000.00);
        
        when(cardRepository.findByCardId("CARD001")).thenReturn(Optional.of(card));
        when(cardRepository.save(any(Card.class))).thenReturn(card);

        // When
        cardService.processPayment("CARD001", paymentAmount);

        // Then
        verify(cardRepository).findByCardId("CARD001");
        verify(cardRepository).save(argThat(c -> c.getAvailableLimit().equals(expectedAvailableLimit)));
    }

    @Test
    void getAvailableLimit_Success() {
        // Given
        when(cardRepository.findByCardId("CARD001")).thenReturn(Optional.of(card));

        // When
        BigDecimal availableLimit = cardService.getAvailableLimit("CARD001");

        // Then
        assertNotNull(availableLimit);
        assertEquals(BigDecimal.valueOf(100000.00), availableLimit);
        verify(cardRepository).findByCardId("CARD001");
    }

    @Test
    void changeCreditLimit_Success() {
        // Given
        BigDecimal newLimit = BigDecimal.valueOf(150000.00);
        when(cardRepository.findByCardId("CARD001")).thenReturn(Optional.of(card));
        when(cardRepository.save(any(Card.class))).thenReturn(card);

        // When
        cardService.changeCreditLimit("CARD001", newLimit);

        // Then
        verify(cardRepository).findByCardId("CARD001");
        verify(cardRepository).save(argThat(c -> c.getCreditLimit().equals(newLimit)));
    }

    @Test
    void generatePin_Success() {
        // Given
        when(cardRepository.findByCardId("CARD001")).thenReturn(Optional.of(card));
        when(cardRepository.save(any(Card.class))).thenReturn(card);

        // When
        String pin = cardService.generatePin("CARD001");

        // Then
        assertNotNull(pin);
        assertEquals(4, pin.length());
        assertTrue(pin.matches("\\d{4}"));
        verify(cardRepository).findByCardId("CARD001");
        verify(cardRepository).save(any(Card.class));
    }

    @Test
    void getCardsByStatus_Success() {
        // Given
        List<Card> activeCards = Arrays.asList(card);
        when(cardRepository.findByStatus(Card.CardStatus.ACTIVE)).thenReturn(activeCards);
        when(cardMapper.toResponse(card)).thenReturn(cardResponse);

        // When
        List<CardResponse> result = cardService.getCardsByStatus(Card.CardStatus.ACTIVE);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(cardRepository).findByStatus(Card.CardStatus.ACTIVE);
    }

    @Test
    void renewCard_Success() {
        // Given
        card.setExpiryDate(LocalDate.now().plusMonths(3)); // Expiring soon
        when(cardRepository.findByCardId("CARD001")).thenReturn(Optional.of(card));
        when(cardRepository.save(any(Card.class))).thenReturn(card);

        // When
        cardService.renewCard("CARD001");

        // Then
        verify(cardRepository).findByCardId("CARD001");
        verify(cardRepository).save(argThat(c -> c.getExpiryDate().isAfter(LocalDate.now().plusYears(4))));
    }

    @Test
    void getOutstandingAmount_Success() {
        // Given
        card.setAvailableLimit(BigDecimal.valueOf(70000.00)); // 30000 used
        when(cardRepository.findByCardId("CARD001")).thenReturn(Optional.of(card));

        // When
        BigDecimal outstanding = cardService.getOutstandingAmount("CARD001");

        // Then
        assertNotNull(outstanding);
        assertEquals(BigDecimal.valueOf(30000.00), outstanding);
        verify(cardRepository).findByCardId("CARD001");
    }
}