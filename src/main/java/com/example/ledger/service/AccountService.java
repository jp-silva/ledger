package com.example.ledger.service;

import com.example.ledger.models.Balance;
import com.example.ledger.models.Deposit;
import com.example.ledger.models.Transaction;
import com.example.ledger.repository.TransactionRepository;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class AccountService {
  private final TransactionRepository transactionRepository;
  private final BalanceService balanceService;

  public AccountService(
      TransactionRepository transactionRepository, BalanceService balanceService) {
    this.transactionRepository = transactionRepository;
    this.balanceService = balanceService;
  }

  public Deposit createDeposit(UUID accountId, Integer depositAmount) {
    var transactionTimestamp = OffsetDateTime.now();

    // get the balance if it exists and update it, otherwise create a new balance. then store it.
    Balance currentBalance = balanceService.getBalance(accountId);
    Balance newBalance = new Balance(depositAmount + currentBalance.amount(), transactionTimestamp);
    balanceService.saveBalance(accountId, newBalance);

    // create and store the transaction
    var transactionUUID = UUID.randomUUID();
    Transaction transaction =
        new Transaction(
            transactionUUID,
            depositAmount,
            Transaction.TransactionType.DEPOSIT,
            transactionTimestamp);
    transactionRepository.addTransaction(transaction);

    return new Deposit(transactionUUID, depositAmount, newBalance.amount(), transactionTimestamp);
  }

  public Deposit createWithdrawal(UUID accountId, Integer withdrawalAmount) {
    var transactionTimestamp = OffsetDateTime.now();

    // get the balance and check if it has sufficient funds
    Balance currentBalance = balanceService.getBalance(accountId);
    if (currentBalance.amount() < withdrawalAmount) {
      throw new IllegalArgumentException(
          "Balance must be greater than or equal to withdrawal amount");
    }
    Balance newBalance =
        new Balance(currentBalance.amount() - withdrawalAmount, OffsetDateTime.now());
    balanceService.saveBalance(accountId, newBalance);

    // create and store the transaction
    var transactionUUID = UUID.randomUUID();
    Transaction transaction =
        new Transaction(
            transactionUUID,
            withdrawalAmount,
            Transaction.TransactionType.WITHDRAWAL,
            transactionTimestamp);
    transactionRepository.addTransaction(transaction);

    return new Deposit(
        transactionUUID, withdrawalAmount, newBalance.amount(), transactionTimestamp);
  }
}
