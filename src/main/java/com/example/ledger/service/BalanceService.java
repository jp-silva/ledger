package com.example.ledger.service;

import com.example.ledger.models.Balance;
import com.example.ledger.repository.BalanceRepository;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class BalanceService {
  private final BalanceRepository balanceRepository;

  public BalanceService(BalanceRepository balanceRepository) {
    this.balanceRepository = balanceRepository;
  }

  public Balance getBalance(UUID accountId) {
    Optional<Balance> balance = balanceRepository.getBalance(accountId);

    if (balance.isEmpty()) {
      Balance newBalance = new Balance(0, OffsetDateTime.now());
      return balanceRepository.saveBalance(accountId, newBalance);
    } else {
      return balance.get();
    }
  }

  public void saveBalance(UUID accountId, Balance newBalance) {
    balanceRepository.saveBalance(accountId, newBalance);
  }
}
