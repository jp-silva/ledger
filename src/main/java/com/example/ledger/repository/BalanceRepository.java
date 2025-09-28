package com.example.ledger.repository;

import com.example.ledger.models.Balance;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class BalanceRepository {
  private final Map<UUID, Balance> accountBalance;

  public BalanceRepository(Map<UUID, Balance> accountBalance) {
    this.accountBalance = accountBalance;
  }

  public Optional<Balance> getBalance(UUID accountId) {
    return Optional.ofNullable(accountBalance.get(accountId));
  }

  public void saveBalance(UUID accountId, Balance newBalance) {
    accountBalance.put(accountId, newBalance);
  }
}
