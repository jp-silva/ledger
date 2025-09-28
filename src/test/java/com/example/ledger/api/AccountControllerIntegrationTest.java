package com.example.ledger.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class AccountControllerIntegrationTest {
  public static final String DEPOSITS_PATH = "/v1/accounts/{accountId}/deposits";
  public static final String WITHDRAWALS_PATH = "/v1/accounts/{accountId}/withdrawals";
  public static final String BALANCE_PATH = "/v1/accounts/{accountId}/balance";

  @Autowired
  private MockMvc mockMvc;

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
        .andExpect(status().isCreated())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").exists())
        .andExpect(jsonPath("$.type").value("DEPOSIT"))
        .andExpect(jsonPath("$.amount").value(depositAmount))
        .andExpect(jsonPath("$.balance").value(depositAmount))
        .andExpect(jsonPath("$.createdAt").exists());
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
            .andExpect(jsonPath("$.balance").value(firstDepositAmount));

    // When - Second deposit
    String secondDepositBody = """
            { "amount": %d }
            """.formatted(secondDepositAmount);
    mockMvc
            .perform(
                    post(DEPOSITS_PATH, accountId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(secondDepositBody))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.amount").value(secondDepositAmount))
            .andExpect(jsonPath("$.balance").value(expectedTotalBalance));
  }


  // WITHDRAWALS
  @Test
  void createWithdrawal_ShouldReturn201_WhenValidWithdrawalRequest() throws Exception {
    // Given
    UUID accountId = UUID.randomUUID();
    Integer initialDepositAmount = 15000; // Amount in cents
    Integer withdrawalAmount = 5000; // Amount in cents
    Integer expectedBalance = initialDepositAmount - withdrawalAmount;

    // Setup initial balance with a deposit
    String depositBody = """
            { "amount": %d }
            """.formatted(initialDepositAmount);
    mockMvc
        .perform(
            post(DEPOSITS_PATH, accountId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(depositBody))
        .andExpect(status().isCreated());

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
        .andExpect(jsonPath("$.balance").value(expectedBalance))
        .andExpect(jsonPath("$.createdAt").exists());
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

    // Setup initial balance with a deposit
    String depositBody = """
            { "amount": %d }
            """.formatted(initialDepositAmount);
    mockMvc
        .perform(
            post(DEPOSITS_PATH, accountId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(depositBody))
        .andExpect(status().isCreated());

    // When - First withdrawal
    String firstWithdrawalBody = """
            { "amount": %d }
            """.formatted(firstWithdrawalAmount);
    mockMvc
            .perform(
                    post(WITHDRAWALS_PATH, accountId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(firstWithdrawalBody))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.balance").value(expectedBalanceAfterFirstWithdrawal));

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
            .andExpect(jsonPath("$.amount").value(secondWithdrawalAmount))
            .andExpect(jsonPath("$.balance").value(expectedFinalBalance));
  }

  // GET BALANCE TESTS
  @Test
  void getBalance_ShouldReturnZeroBalance_WhenAccountHasNoTransactions() throws Exception {
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
  void getBalance_ShouldReturnCorrectBalance_AfterDeposit() throws Exception {
    // Given
    UUID accountId = UUID.randomUUID();
    Integer depositAmount = 12500;

    // Setup balance with a deposit
    String depositBody = """
            { "amount": %d }
            """.formatted(depositAmount);
    mockMvc
        .perform(
            post(DEPOSITS_PATH, accountId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(depositBody))
        .andExpect(status().isCreated());

    // When & Then
    mockMvc
        .perform(get(BALANCE_PATH, accountId))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.amount").value(depositAmount));
  }

  @Test
  void getBalance_ShouldReturnCorrectBalance_AfterMultipleDeposits() throws Exception {
    // Given
    UUID accountId = UUID.randomUUID();
    Integer firstDepositAmount = 8000;
    Integer secondDepositAmount = 4500;
    Integer expectedTotalBalance = firstDepositAmount + secondDepositAmount;

    // Setup balance with multiple deposits
    String firstDepositBody = """
            { "amount": %d }
            """.formatted(firstDepositAmount);
    mockMvc
        .perform(
            post(DEPOSITS_PATH, accountId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(firstDepositBody))
        .andExpect(status().isCreated());

    String secondDepositBody = """
            { "amount": %d }
            """.formatted(secondDepositAmount);
    mockMvc
        .perform(
            post(DEPOSITS_PATH, accountId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(secondDepositBody))
        .andExpect(status().isCreated());

    // When & Then
    mockMvc
        .perform(get(BALANCE_PATH, accountId))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.amount").value(expectedTotalBalance));
  }

  @Test
  void getBalance_ShouldReturnCorrectBalance_AfterDepositAndWithdrawal() throws Exception {
    // Given
    UUID accountId = UUID.randomUUID();
    Integer depositAmount = 20000;
    Integer withdrawalAmount = 7500;
    Integer expectedBalance = depositAmount - withdrawalAmount;

    // Setup balance with deposit and withdrawal
    String depositBody = """
            { "amount": %d }
            """.formatted(depositAmount);
    mockMvc
        .perform(
            post(DEPOSITS_PATH, accountId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(depositBody))
        .andExpect(status().isCreated());

    String withdrawalBody = """
            { "amount": %d }
            """.formatted(withdrawalAmount);
    mockMvc
        .perform(
            post(WITHDRAWALS_PATH, accountId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(withdrawalBody))
        .andExpect(status().isCreated());

    // When & Then
    mockMvc
        .perform(get(BALANCE_PATH, accountId))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.amount").value(expectedBalance));
  }

  @Test
  void getBalance_ShouldReturnCorrectBalance_AfterComplexTransactionHistory() throws Exception {
    // Given
    UUID accountId = UUID.randomUUID();
    Integer firstDepositAmount = 15000;
    Integer secondDepositAmount = 10000;
    Integer firstWithdrawalAmount = 5000;
    Integer thirdDepositAmount = 3000;
    Integer secondWithdrawalAmount = 2000;
    Integer expectedFinalBalance = firstDepositAmount + secondDepositAmount - firstWithdrawalAmount + thirdDepositAmount - secondWithdrawalAmount;

    // Setup complex transaction history
    // First deposit
    String firstDepositBody = """
            { "amount": %d }
            """.formatted(firstDepositAmount);
    mockMvc.perform(post(DEPOSITS_PATH, accountId).contentType(MediaType.APPLICATION_JSON).content(firstDepositBody)).andExpect(status().isCreated());

    // Second deposit
    String secondDepositBody = """
            { "amount": %d }
            """.formatted(secondDepositAmount);
    mockMvc.perform(post(DEPOSITS_PATH, accountId).contentType(MediaType.APPLICATION_JSON).content(secondDepositBody)).andExpect(status().isCreated());

    // First withdrawal
    String firstWithdrawalBody = """
            { "amount": %d }
            """.formatted(firstWithdrawalAmount);
    mockMvc.perform(post(WITHDRAWALS_PATH, accountId).contentType(MediaType.APPLICATION_JSON).content(firstWithdrawalBody)).andExpect(status().isCreated());

    // Third deposit
    String thirdDepositBody = """
            { "amount": %d }
            """.formatted(thirdDepositAmount);
    mockMvc.perform(post(DEPOSITS_PATH, accountId).contentType(MediaType.APPLICATION_JSON).content(thirdDepositBody)).andExpect(status().isCreated());

    // Second withdrawal
    String secondWithdrawalBody = """
            { "amount": %d }
            """.formatted(secondWithdrawalAmount);
    mockMvc.perform(post(WITHDRAWALS_PATH, accountId).contentType(MediaType.APPLICATION_JSON).content(secondWithdrawalBody)).andExpect(status().isCreated());

    // When & Then
    mockMvc
        .perform(get(BALANCE_PATH, accountId))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.amount").value(expectedFinalBalance));
  }
}
