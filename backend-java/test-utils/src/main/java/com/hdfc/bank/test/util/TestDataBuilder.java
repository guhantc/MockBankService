package com.hdfc.bank.test.util;

import com.hdfc.bank.user.model.User;
import com.hdfc.bank.account.model.Account;
import com.hdfc.bank.loan.model.Loan;
import com.hdfc.bank.card.model.Card;
import com.hdfc.bank.transaction.model.Transaction;
import com.hdfc.bank.investment.model.Investment;
import com.hdfc.bank.notification.model.Notification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Test Data Builder for creating test entities
 */
public class TestDataBuilder {

    public static class UserTestDataBuilder {
        private User user;

        public UserTestDataBuilder() {
            this.user = new User();
            // Set default values
            this.user.setUserId("USER" + System.currentTimeMillis());
            this.user.setFirstName("John");
            this.user.setLastName("Doe");
            this.user.setEmail("john.doe@test.com");
            this.user.setPhoneNumber("9876543210");
            this.user.setPassword("password123");
            this.user.setDateOfBirth(LocalDate.of(1990, 1, 1));
            this.user.setStatus(User.UserStatus.ACTIVE);
            this.user.setRole(User.UserRole.CUSTOMER);
            this.user.setCreatedAt(LocalDateTime.now());
            this.user.setUpdatedAt(LocalDateTime.now());
        }

        public UserTestDataBuilder withUserId(String userId) {
            this.user.setUserId(userId);
            return this;
        }

        public UserTestDataBuilder withName(String firstName, String lastName) {
            this.user.setFirstName(firstName);
            this.user.setLastName(lastName);
            return this;
        }

        public UserTestDataBuilder withEmail(String email) {
            this.user.setEmail(email);
            return this;
        }

        public UserTestDataBuilder withPhoneNumber(String phoneNumber) {
            this.user.setPhoneNumber(phoneNumber);
            return this;
        }

        public UserTestDataBuilder withStatus(User.UserStatus status) {
            this.user.setStatus(status);
            return this;
        }

        public UserTestDataBuilder withRole(User.UserRole role) {
            this.user.setRole(role);
            return this;
        }

        public UserTestDataBuilder withDateOfBirth(LocalDate dateOfBirth) {
            this.user.setDateOfBirth(dateOfBirth);
            return this;
        }

        public User build() {
            return this.user;
        }
    }

    public static class AccountTestDataBuilder {
        private Account account;

        public AccountTestDataBuilder() {
            this.account = new Account();
            // Set default values
            this.account.setAccountId("ACC" + System.currentTimeMillis());
            this.account.setUserId("USER001");
            this.account.setAccountType(Account.AccountType.SAVINGS);
            this.account.setBalance(BigDecimal.valueOf(10000.00));
            this.account.setStatus(Account.AccountStatus.ACTIVE);
            this.account.setCreatedAt(LocalDateTime.now());
            this.account.setUpdatedAt(LocalDateTime.now());
        }

        public AccountTestDataBuilder withAccountId(String accountId) {
            this.account.setAccountId(accountId);
            return this;
        }

        public AccountTestDataBuilder withUserId(String userId) {
            this.account.setUserId(userId);
            return this;
        }

        public AccountTestDataBuilder withAccountType(Account.AccountType accountType) {
            this.account.setAccountType(accountType);
            return this;
        }

        public AccountTestDataBuilder withBalance(BigDecimal balance) {
            this.account.setBalance(balance);
            return this;
        }

        public AccountTestDataBuilder withStatus(Account.AccountStatus status) {
            this.account.setStatus(status);
            return this;
        }

        public Account build() {
            return this.account;
        }
    }

    public static class LoanTestDataBuilder {
        private Loan loan;

        public LoanTestDataBuilder() {
            this.loan = new Loan();
            // Set default values
            this.loan.setLoanId("LOAN" + System.currentTimeMillis());
            this.loan.setUserId("USER001");
            this.loan.setAccountId("ACC001");
            this.loan.setLoanType(Loan.LoanType.HOME_LOAN);
            this.loan.setPrincipalAmount(BigDecimal.valueOf(500000.00));
            this.loan.setInterestRate(BigDecimal.valueOf(8.5));
            this.loan.setTenureMonths(240);
            this.loan.setEmiAmount(BigDecimal.valueOf(4386.24));
            this.loan.setStatus(Loan.LoanStatus.PENDING);
            this.loan.setApplicationDate(LocalDate.now());
            this.loan.setCreatedAt(LocalDateTime.now());
        }

        public LoanTestDataBuilder withLoanId(String loanId) {
            this.loan.setLoanId(loanId);
            return this;
        }

        public LoanTestDataBuilder withUserId(String userId) {
            this.loan.setUserId(userId);
            return this;
        }

        public LoanTestDataBuilder withLoanType(Loan.LoanType loanType) {
            this.loan.setLoanType(loanType);
            return this;
        }

        public LoanTestDataBuilder withPrincipalAmount(BigDecimal principalAmount) {
            this.loan.setPrincipalAmount(principalAmount);
            return this;
        }

        public LoanTestDataBuilder withStatus(Loan.LoanStatus status) {
            this.loan.setStatus(status);
            return this;
        }

        public LoanTestDataBuilder withTenure(int tenureMonths) {
            this.loan.setTenureMonths(tenureMonths);
            return this;
        }

        public Loan build() {
            return this.loan;
        }
    }

    public static class CardTestDataBuilder {
        private Card card;

        public CardTestDataBuilder() {
            this.card = new Card();
            // Set default values
            this.card.setCardId("CARD" + System.currentTimeMillis());
            this.card.setUserId("USER001");
            this.card.setAccountId("ACC001");
            this.card.setCardNumber("4567-1234-5678-9012");
            this.card.setCardType(Card.CardType.CREDIT_CARD);
            this.card.setCardCategory(Card.CardCategory.PLATINUM);
            this.card.setCreditLimit(BigDecimal.valueOf(100000.00));
            this.card.setAvailableLimit(BigDecimal.valueOf(100000.00));
            this.card.setExpiryDate(LocalDate.now().plusYears(5));
            this.card.setStatus(Card.CardStatus.ACTIVE);
            this.card.setCreatedAt(LocalDateTime.now());
        }

        public CardTestDataBuilder withCardId(String cardId) {
            this.card.setCardId(cardId);
            return this;
        }

        public CardTestDataBuilder withUserId(String userId) {
            this.card.setUserId(userId);
            return this;
        }

        public CardTestDataBuilder withCardType(Card.CardType cardType) {
            this.card.setCardType(cardType);
            return this;
        }

        public CardTestDataBuilder withCreditLimit(BigDecimal creditLimit) {
            this.card.setCreditLimit(creditLimit);
            return this;
        }

        public CardTestDataBuilder withStatus(Card.CardStatus status) {
            this.card.setStatus(status);
            return this;
        }

        public Card build() {
            return this.card;
        }
    }

    public static class TransactionTestDataBuilder {
        private Transaction transaction;

        public TransactionTestDataBuilder() {
            this.transaction = new Transaction();
            // Set default values
            this.transaction.setTransactionId("TXN" + System.currentTimeMillis());
            this.transaction.setSourceAccountId("ACC001");
            this.transaction.setTargetAccountId("ACC002");
            this.transaction.setAmount(BigDecimal.valueOf(5000.00));
            this.transaction.setTransactionType(Transaction.TransactionType.TRANSFER);
            this.transaction.setStatus(Transaction.TransactionStatus.COMPLETED);
            this.transaction.setDescription("Test transfer");
            this.transaction.setCreatedAt(LocalDateTime.now());
        }

        public TransactionTestDataBuilder withTransactionId(String transactionId) {
            this.transaction.setTransactionId(transactionId);
            return this;
        }

        public TransactionTestDataBuilder withSourceAccount(String sourceAccountId) {
            this.transaction.setSourceAccountId(sourceAccountId);
            return this;
        }

        public TransactionTestDataBuilder withTargetAccount(String targetAccountId) {
            this.transaction.setTargetAccountId(targetAccountId);
            return this;
        }

        public TransactionTestDataBuilder withAmount(BigDecimal amount) {
            this.transaction.setAmount(amount);
            return this;
        }

        public TransactionTestDataBuilder withType(Transaction.TransactionType type) {
            this.transaction.setTransactionType(type);
            return this;
        }

        public TransactionTestDataBuilder withStatus(Transaction.TransactionStatus status) {
            this.transaction.setStatus(status);
            return this;
        }

        public Transaction build() {
            return this.transaction;
        }
    }

    public static class InvestmentTestDataBuilder {
        private Investment investment;

        public InvestmentTestDataBuilder() {
            this.investment = new Investment();
            // Set default values
            this.investment.setInvestmentId("INV" + System.currentTimeMillis());
            this.investment.setUserId("USER001");
            this.investment.setAccountId("ACC001");
            this.investment.setInvestmentType(Investment.InvestmentType.MUTUAL_FUND);
            this.investment.setProductCode("MF001");
            this.investment.setProductName("HDFC Equity Fund");
            this.investment.setInvestedAmount(BigDecimal.valueOf(50000.00));
            this.investment.setCurrentValue(BigDecimal.valueOf(55000.00));
            this.investment.setUnits(BigDecimal.valueOf(1000.00));
            this.investment.setNavPrice(BigDecimal.valueOf(55.00));
            this.investment.setStatus(Investment.InvestmentStatus.ACTIVE);
            this.investment.setPurchaseDate(LocalDate.now());
            this.investment.setCreatedAt(LocalDateTime.now());
        }

        public InvestmentTestDataBuilder withInvestmentId(String investmentId) {
            this.investment.setInvestmentId(investmentId);
            return this;
        }

        public InvestmentTestDataBuilder withUserId(String userId) {
            this.investment.setUserId(userId);
            return this;
        }

        public InvestmentTestDataBuilder withInvestmentType(Investment.InvestmentType type) {
            this.investment.setInvestmentType(type);
            return this;
        }

        public InvestmentTestDataBuilder withInvestedAmount(BigDecimal amount) {
            this.investment.setInvestedAmount(amount);
            return this;
        }

        public InvestmentTestDataBuilder withStatus(Investment.InvestmentStatus status) {
            this.investment.setStatus(status);
            return this;
        }

        public Investment build() {
            return this.investment;
        }
    }

    public static class NotificationTestDataBuilder {
        private Notification notification;

        public NotificationTestDataBuilder() {
            this.notification = new Notification();
            // Set default values
            this.notification.setNotificationId("NOTIF" + System.currentTimeMillis());
            this.notification.setUserId("USER001");
            this.notification.setType(Notification.NotificationType.TRANSACTION_ALERT);
            this.notification.setChannel(Notification.NotificationChannel.SMS);
            this.notification.setTitle("Transaction Alert");
            this.notification.setMessage("Your account has been debited");
            this.notification.setStatus(Notification.NotificationStatus.SENT);
            this.notification.setRecipient("9876543210");
            this.notification.setCreatedAt(LocalDateTime.now());
        }

        public NotificationTestDataBuilder withNotificationId(String notificationId) {
            this.notification.setNotificationId(notificationId);
            return this;
        }

        public NotificationTestDataBuilder withUserId(String userId) {
            this.notification.setUserId(userId);
            return this;
        }

        public NotificationTestDataBuilder withType(Notification.NotificationType type) {
            this.notification.setType(type);
            return this;
        }

        public NotificationTestDataBuilder withChannel(Notification.NotificationChannel channel) {
            this.notification.setChannel(channel);
            return this;
        }

        public NotificationTestDataBuilder withStatus(Notification.NotificationStatus status) {
            this.notification.setStatus(status);
            return this;
        }

        public Notification build() {
            return this.notification;
        }
    }

    // Builder factory methods
    public static UserTestDataBuilder createUser() {
        return new UserTestDataBuilder();
    }

    public static AccountTestDataBuilder createAccount() {
        return new AccountTestDataBuilder();
    }

    public static LoanTestDataBuilder createLoan() {
        return new LoanTestDataBuilder();
    }

    public static CardTestDataBuilder createCard() {
        return new CardTestDataBuilder();
    }

    public static TransactionTestDataBuilder createTransaction() {
        return new TransactionTestDataBuilder();
    }

    public static InvestmentTestDataBuilder createInvestment() {
        return new InvestmentTestDataBuilder();
    }

    public static NotificationTestDataBuilder createNotification() {
        return new NotificationTestDataBuilder();
    }
}