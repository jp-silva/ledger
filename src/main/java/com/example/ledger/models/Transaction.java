package com.example.ledger.models;

import java.time.OffsetDateTime;
import java.util.UUID;

public record Transaction(
    UUID id,
    UUID accountId,
    Integer amount,
    TransactionType type,
    Integer balance,
    OffsetDateTime createdAt) {
  public enum TransactionType {
    DEPOSIT,
    WITHDRAWAL
  }
}
