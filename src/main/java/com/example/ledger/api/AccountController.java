package com.example.ledger.api;

import com.example.ledger.generated.api.AccountApi;
import com.example.ledger.generated.model.Balance;
import com.example.ledger.generated.model.Transaction;
import com.example.ledger.generated.model.TransactionRequest;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AccountController implements AccountApi {
  @Override
  public ResponseEntity<Transaction> createDeposit(
      UUID accountId, TransactionRequest transactionRequest) {
    return AccountApi.super.createDeposit(accountId, transactionRequest);
  }

  @Override
  public ResponseEntity<Transaction> createWithdrawal(
      UUID accountId, TransactionRequest transactionRequest) {
    return AccountApi.super.createWithdrawal(accountId, transactionRequest);
  }

  @Override
  public ResponseEntity<Balance> getBalance(UUID accountId) {
    return AccountApi.super.getBalance(accountId);
  }

  @Override
  public ResponseEntity<List<Transaction>> getTransactions(UUID accountId) {
    return AccountApi.super.getTransactions(accountId);
  }
}
