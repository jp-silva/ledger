package com.example.ledger.api;

import com.example.ledger.models.Balance;
import com.example.ledger.models.Transaction;
import com.example.ledger.repository.BalanceRepository;
import com.example.ledger.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class AccountControllerIntegrationTest {
  public static final String DEPOSITS_PATH = "/v1/accounts/{accountId}/deposits";
  public static final String WITHDRAWALS_PATH = "/v1/accounts/{accountId}/withdrawals";
  public static final String BALANCE_PATH = "/v1/accounts/{accountId}/balance";
  public static final String TRANSACTIONS_PATH = "/v1/accounts/{accountId}/transactions";

  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private BalanceRepository balanceRepository;
  @Autowired
  private TransactionRepository transactionRepository;

  @Test
  void createDeposit_ShouldReturn201_WhenValidDepositRequest() throws Exception {
    // Given
    UUID accountId = UUID.randomUUID();
    Integer depositAmount = 10000; // Amount in cents
    String requestBody = """
            { "amount": %d }
            """.formatted(depositAmount);

    // When & Then
    mockMvc
        .perform(
            post(DEPOSITS_PATH, accountId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andDo(print())
        .andExpect(status().isCreated())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").exists())
        .andExpect(jsonPath("$.type").value("DEPOSIT"))
        .andExpect(jsonPath("$.amount").value(depositAmount))
        .andExpect(jsonPath("$.createdAt").exists());
    assertBalance(accountId, depositAmount);
  }

  @Test
  void createDeposit_ShouldAccumulateBalance_WhenMultipleDeposits() throws Exception {
    // Given
    UUID accountId = UUID.randomUUID();
    Integer firstDepositAmount = 5000;
    Integer secondDepositAmount = 3000;
    Integer expectedTotalBalance = firstDepositAmount + secondDepositAmount;

    // When - First deposit
    String firstDepositBody = """
            { "amount": %d }
            """.formatted(firstDepositAmount);
    mockMvc
            .perform(
                    post(DEPOSITS_PATH, accountId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(firstDepositBody))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.amount").value(firstDepositAmount));
    assertBalance(accountId, firstDepositAmount);

    // When - Second deposit
    String secondDepositBody = """
            { "amount": %d }
            """.formatted(secondDepositAmount);
    mockMvc
            .perform(
                    post(DEPOSITS_PATH, accountId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(secondDepositBody))
            .andDo(print())
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.amount").value(secondDepositAmount));
    assertBalance(accountId, expectedTotalBalance);
  }


  // WITHDRAWALS
  @Test
  void createWithdrawal_ShouldReturn201_WhenValidWithdrawalRequest() throws Exception {
    // Given
    UUID accountId = UUID.randomUUID();
    Integer initialDepositAmount = 15000; // Amount in cents
    Integer withdrawalAmount = 5000; // Amount in cents
    Integer expectedBalance = initialDepositAmount - withdrawalAmount;

    // Setup initial balance
    setInitialBalance(accountId, initialDepositAmount);

    // When - Create withdrawal
    String requestBody = """
            { "amount": %d }
            """.formatted(withdrawalAmount);

    // Then
    mockMvc
        .perform(
            post(WITHDRAWALS_PATH, accountId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
        .andExpect(status().isCreated())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").exists())
        .andExpect(jsonPath("$.type").value("WITHDRAWAL"))
        .andExpect(jsonPath("$.amount").value(withdrawalAmount))
        .andExpect(jsonPath("$.createdAt").exists());
    assertBalance(accountId, expectedBalance);
  }

  @Test
  void createWithdrawal_ShouldDeductFromBalance_WhenMultipleWithdrawals() throws Exception {
    // Given
    UUID accountId = UUID.randomUUID();
    Integer initialDepositAmount = 20000;
    Integer firstWithdrawalAmount = 3000;
    Integer secondWithdrawalAmount = 2000;
    Integer expectedBalanceAfterFirstWithdrawal = initialDepositAmount - firstWithdrawalAmount;
    Integer expectedFinalBalance = initialDepositAmount - firstWithdrawalAmount - secondWithdrawalAmount;

    // Setup initial balance
    setInitialBalance(accountId, initialDepositAmount);

    // When - First withdrawal
    String firstWithdrawalBody = """
            { "amount": %d }
            """.formatted(firstWithdrawalAmount);
    mockMvc
            .perform(
                    post(WITHDRAWALS_PATH, accountId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(firstWithdrawalBody))
            .andExpect(status().isCreated());
  assertBalance(accountId, expectedBalanceAfterFirstWithdrawal);

    // When - Second withdrawal
    String secondWithdrawalBody = """
            { "amount": %d }
            """.formatted(secondWithdrawalAmount);
    mockMvc
            .perform(
                    post(WITHDRAWALS_PATH, accountId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(secondWithdrawalBody))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.amount").value(secondWithdrawalAmount));
    assertBalance(accountId, expectedFinalBalance);
  }

  @Test
  void createWithdrawal_ShouldReturn400_WhenInsufficientBalance() throws Exception {
    // Given
    UUID accountId = UUID.randomUUID();
    Integer initialDepositAmount = 5000; // Amount in cents
    Integer withdrawalAmount = 10000; // Amount greater than balance

    // Setup initial balance
    setInitialBalance(accountId, initialDepositAmount);

    // When - Attempt withdrawal with insufficient balance
    String requestBody = """
            { "amount": %d }
            """.formatted(withdrawalAmount);

    // Then - Should return 400 Bad Request
    mockMvc
        .perform(
            post(WITHDRAWALS_PATH, accountId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
        .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Balance must be greater than or equal to withdrawal amount"));;

    // Balance should remain unchanged
    assertBalance(accountId, initialDepositAmount);
  }

  // GET BALANCE TESTS
  @Test
  void getBalance_ShouldReturnZeroBalance_WhenBalanceWasNotInitiated() throws Exception {
    // Given
    UUID accountId = UUID.randomUUID();

    // When & Then
    mockMvc
        .perform(get(BALANCE_PATH, accountId))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.amount").value(0));
  }

  @Test
  void getBalance_ShouldReturnCorrectBalance() throws Exception {
    // Given
    UUID accountId = UUID.randomUUID();
    Integer depositAmount = 12500;

    // Setup balance
    setInitialBalance(accountId, depositAmount);

    // When & Then
    mockMvc
        .perform(get(BALANCE_PATH, accountId))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.amount").value(depositAmount));
  }

  // GET TRANSACTIONS TESTS
  @Test
  void getTransactions_ShouldReturnEmptyList_WhenNoTransactionsExist() throws Exception {
    // Given
    UUID accountId = UUID.randomUUID();

    // When & Then
    mockMvc
        .perform(get(TRANSACTIONS_PATH, accountId))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.transactions").isArray())
        .andExpect(jsonPath("$.transactions").isEmpty());
  }

  @Test
  void getTransactions_ShouldReturnSingleTransaction_WhenOneTransactionExists() throws Exception {
    // Given
    UUID accountId = UUID.randomUUID();
    Integer depositAmount = 10000;
    var transaction = new Transaction(UUID.randomUUID(), accountId, depositAmount, Transaction.TransactionType.DEPOSIT, OffsetDateTime.now());
    transactionRepository.addTransaction(transaction);

    // When & Then
    mockMvc
        .perform(get(TRANSACTIONS_PATH, accountId))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.transactions").isArray())
        .andExpect(jsonPath("$.transactions").isNotEmpty())
        .andExpect(jsonPath("$.transactions[0].id").exists())
        .andExpect(jsonPath("$.transactions[0].type").value("DEPOSIT"))
        .andExpect(jsonPath("$.transactions[0].amount").value(depositAmount))
        .andExpect(jsonPath("$.transactions[0].createdAt").exists());
  }

  @Test
  void getTransactions_ShouldReturnMultipleTransactions_WhenMultipleTransactionsExist() throws Exception {
    // Given
    UUID accountId = UUID.randomUUID();
    Integer firstDepositAmount = 15000;
    Integer withdrawalAmount = 5000;
    Integer secondDepositAmount = 8000;
    var transaction = new Transaction(UUID.randomUUID(), accountId, firstDepositAmount, Transaction.TransactionType.DEPOSIT, OffsetDateTime.now());
    transactionRepository.addTransaction(transaction);
    transaction = new Transaction(UUID.randomUUID(), accountId, withdrawalAmount, Transaction.TransactionType.WITHDRAWAL, OffsetDateTime.now());
    transactionRepository.addTransaction(transaction);
    transaction = new Transaction(UUID.randomUUID(), accountId, secondDepositAmount, Transaction.TransactionType.DEPOSIT, OffsetDateTime.now());
    transactionRepository.addTransaction(transaction);

    // When & Then
    mockMvc
        .perform(get(TRANSACTIONS_PATH, accountId))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.transactions").isArray())
        .andExpect(jsonPath("$.transactions.length()").value(3))
        .andExpect(jsonPath("$.transactions[0].type").value("DEPOSIT"))
        .andExpect(jsonPath("$.transactions[0].amount").value(firstDepositAmount))
        .andExpect(jsonPath("$.transactions[1].type").value("WITHDRAWAL"))
        .andExpect(jsonPath("$.transactions[1].amount").value(withdrawalAmount))
        .andExpect(jsonPath("$.transactions[2].type").value("DEPOSIT"))
        .andExpect(jsonPath("$.transactions[2].amount").value(secondDepositAmount));
  }

  @Test
  void getTransactions_ShouldReturnOnlyAccountSpecificTransactions() throws Exception {
    // Given
    UUID firstAccountId = UUID.randomUUID();
    UUID secondAccountId = UUID.randomUUID();
    Integer firstAccountDepositAmount = 12000;
    Integer secondAccountDepositAmount = 8000;
    var transaction = new Transaction(UUID.randomUUID(), firstAccountId, firstAccountDepositAmount, Transaction.TransactionType.WITHDRAWAL, OffsetDateTime.now());
    transactionRepository.addTransaction(transaction);
    transaction = new Transaction(UUID.randomUUID(), secondAccountId, secondAccountDepositAmount, Transaction.TransactionType.WITHDRAWAL, OffsetDateTime.now());
    transactionRepository.addTransaction(transaction);

    // When - Get transactions for first account
    mockMvc
        .perform(get(TRANSACTIONS_PATH, firstAccountId))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.transactions").isArray())
        .andExpect(jsonPath("$.transactions.length()").value(1))
        .andExpect(jsonPath("$.transactions[0].amount").value(firstAccountDepositAmount));

    // Then - Get transactions for second account
    mockMvc
        .perform(get(TRANSACTIONS_PATH, secondAccountId))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.transactions").isArray())
        .andExpect(jsonPath("$.transactions.length()").value(1))
        .andExpect(jsonPath("$.transactions[0].amount").value(secondAccountDepositAmount));
  }

  private void assertBalance(UUID accountId, Integer amount) {
    assertTrue(balanceRepository.getBalance(accountId).isPresent());
    assertEquals(amount, balanceRepository.getBalance(accountId).get().amount());
  }

  private void setInitialBalance(UUID accountId, Integer amount) {
    balanceRepository.saveBalance(accountId, new Balance(amount, OffsetDateTime.now()));
  }
}
