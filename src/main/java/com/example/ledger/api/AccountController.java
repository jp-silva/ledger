package com.example.ledger.api;

import com.example.ledger.generated.api.AccountApi;
import com.example.ledger.generated.model.DepositRequest;
import com.example.ledger.generated.model.TransactionResponse;
import com.example.ledger.mappers.AccountMapper;
import com.example.ledger.models.Deposit;
import com.example.ledger.service.AccountService;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AccountController implements AccountApi {

  private final AccountService accountService;
  private final AccountMapper accountMapper;

  public AccountController(AccountService accountService, AccountMapper accountMapper) {
    this.accountService = accountService;
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
}
