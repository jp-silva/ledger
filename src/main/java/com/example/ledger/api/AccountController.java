package com.example.ledger.api;

import com.example.ledger.generated.api.AccountApi;
import com.example.ledger.generated.model.BalanceResponse;
import com.example.ledger.generated.model.DepositRequest;
import com.example.ledger.generated.model.TransactionResponse;
import com.example.ledger.generated.model.TransactionsListResponse;
import com.example.ledger.generated.model.WithdrawalRequest;
import com.example.ledger.mappers.AccountMapper;
import com.example.ledger.models.Balance;
import com.example.ledger.models.Transaction;
import com.example.ledger.service.BalanceService;
import com.example.ledger.service.TransactionService;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AccountController implements AccountApi {

  private final TransactionService transactionService;
  private final BalanceService balanceService;
  private final AccountMapper accountMapper;

  public AccountController(
      TransactionService transactionService,
      BalanceService balanceService,
      AccountMapper accountMapper) {
    this.transactionService = transactionService;
    this.balanceService = balanceService;
    this.accountMapper = accountMapper;
  }

  @Override
  public ResponseEntity<TransactionResponse> createDeposit(
      UUID accountId, DepositRequest depositRequest) {

    Transaction transaction =
        transactionService.createDeposit(accountId, depositRequest.getAmount());

    return ResponseEntity.status(201).body(accountMapper.toTransactionResponse(transaction));
  }

  @Override
  public ResponseEntity<TransactionResponse> createWithdrawal(
      UUID accountId, WithdrawalRequest withdrawalRequest) {
    Transaction transaction =
        transactionService.createWithdrawal(accountId, withdrawalRequest.getAmount());

    return ResponseEntity.status(201).body(accountMapper.toTransactionResponse(transaction));
  }

  @Override
  public ResponseEntity<BalanceResponse> getBalance(UUID accountId) {
    Balance balance = balanceService.getBalance(accountId);
    return ResponseEntity.ok(accountMapper.toBalanceResponse(balance));
  }

  @Override
  public ResponseEntity<TransactionsListResponse> getTransactions(UUID accountId) {
    List<TransactionResponse> transactions =
        transactionService.getTransactions(accountId).stream()
            .map(accountMapper::toTransactionResponse)
            .toList();

    TransactionsListResponse transactionsListResponse = new TransactionsListResponse();
    transactionsListResponse.setTransactions(transactions);

    return ResponseEntity.ok(transactionsListResponse);
  }
}
