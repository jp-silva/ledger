package com.example.ledger.repository;

import com.example.ledger.models.Transaction;
import java.util.List;

public class TransactionRepository {
  private final List<String> transactions;

  public TransactionRepository(List<String> transactions) {
    this.transactions = transactions;
  }

  public void addTransaction(Transaction transaction) {}
}
