package com.example.ledger.models;

import java.time.OffsetDateTime;
import java.util.UUID;

public record Transaction(UUID id, Integer amount, TransactionType type, OffsetDateTime createdAt) {
  public enum TransactionType {
    DEPOSIT,
    WITHDRAWAL
  }
}
