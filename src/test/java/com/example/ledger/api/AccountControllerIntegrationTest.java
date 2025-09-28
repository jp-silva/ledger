package com.example.ledger.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class AccountControllerIntegrationTest {
  public static final String DEPOSITS_PATH = "/v1/accounts/{accountId}/deposits";

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
}
