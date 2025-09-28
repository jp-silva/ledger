package com.example.ledger.service;

import com.example.ledger.models.Balance;
import com.example.ledger.models.Deposit;
import com.example.ledger.models.Transaction;
import com.example.ledger.repository.BalanceRepository;
import com.example.ledger.repository.TransactionRepository;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class AccountService {
  private final TransactionRepository transactionRepository;
  private final BalanceRepository balanceRepository;

  public AccountService(
      TransactionRepository transactionRepository, BalanceRepository balanceRepository) {
    this.transactionRepository = transactionRepository;
    this.balanceRepository = balanceRepository;
  }

  public Deposit createDeposit(UUID accountId, Integer depositAmount) {
    var transactionUUID = UUID.randomUUID();
    var transactionTimestamp = OffsetDateTime.now();

    // create and store the transaction
    Transaction transaction =
        new Transaction(
            transactionUUID,
            depositAmount,
            Transaction.TransactionType.DEPOSIT,
            transactionTimestamp);
    transactionRepository.addTransaction(transaction);

    // get the balance if it exists and update it, otherwise create a new balance. then store it.
    Balance newBalance =
        balanceRepository
            .getBalance(accountId)
            .map(
                currentBalance ->
                    new Balance(depositAmount + currentBalance.amount(), transactionTimestamp))
            .orElse(new Balance(depositAmount, transactionTimestamp));
    balanceRepository.saveBalance(accountId, newBalance);

    return new Deposit(transactionUUID, depositAmount, newBalance.amount(), transactionTimestamp);
  }
}
