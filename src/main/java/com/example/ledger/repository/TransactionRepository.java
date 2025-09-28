package com.example.ledger.repository;

import com.example.ledger.models.Transaction;
import java.util.List;
import java.util.UUID;

public class TransactionRepository {
  private final List<Transaction> transactions;

  public TransactionRepository(List<Transaction> transactions) {
    this.transactions = transactions;
  }

  public Transaction addTransaction(Transaction transaction) {
    transactions.add(transaction);
    return transaction;
  }

  public List<Transaction> getTransactions(UUID accountId) {
    return transactions.stream()
        .filter(transaction -> transaction.accountId().equals(accountId))
        .toList();
  }
}
