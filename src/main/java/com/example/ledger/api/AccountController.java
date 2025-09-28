package com.example.ledger.api;

import com.example.ledger.generated.api.AccountApi;
import com.example.ledger.generated.model.BalanceResponse;
import com.example.ledger.generated.model.DepositRequest;
import com.example.ledger.generated.model.TransactionResponse;
import com.example.ledger.generated.model.WithdrawalRequest;
import com.example.ledger.mappers.AccountMapper;
import com.example.ledger.models.Balance;
import com.example.ledger.models.Deposit;
import com.example.ledger.service.AccountService;
import com.example.ledger.service.BalanceService;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AccountController implements AccountApi {

  private final AccountService accountService;
  private final BalanceService balanceService;
  private final AccountMapper accountMapper;

  public AccountController(
      AccountService accountService, BalanceService balanceService, AccountMapper accountMapper) {
    this.accountService = accountService;
    this.balanceService = balanceService;
    this.accountMapper = accountMapper;
  }

  @Override
  public ResponseEntity<TransactionResponse> createDeposit(
      UUID accountId, DepositRequest depositRequest) {

    Deposit deposit = accountService.createDeposit(accountId, depositRequest.getAmount());
    TransactionResponse transaction =
        accountMapper.toTransactionResponse(deposit, TransactionResponse.TypeEnum.DEPOSIT);

    return ResponseEntity.status(201).body(transaction);
  }

  @Override
  public ResponseEntity<TransactionResponse> createWithdrawal(
      UUID accountId, WithdrawalRequest withdrawalRequest) {
    Deposit deposit = accountService.createWithdrawal(accountId, withdrawalRequest.getAmount());
    TransactionResponse transaction =
        accountMapper.toTransactionResponse(deposit, TransactionResponse.TypeEnum.WITHDRAWAL);

    return ResponseEntity.status(201).body(transaction);
  }

  @Override
  public ResponseEntity<BalanceResponse> getBalance(UUID accountId) {
    Balance balance = balanceService.getBalance(accountId);
    return ResponseEntity.ok(accountMapper.toBalanceResponse(balance));
  }
}
