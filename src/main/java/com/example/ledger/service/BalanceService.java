package com.example.ledger.service;

import com.example.ledger.models.Balance;
import com.example.ledger.repository.BalanceRepository;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class BalanceService {
  private final BalanceRepository balanceRepository;

  public BalanceService(BalanceRepository balanceRepository) {
    this.balanceRepository = balanceRepository;
  }

  public Balance getBalance(UUID accountId) {
    // get the balance if it exists and update it, otherwise create a new balance. then store it.
    Balance newBalance =
        balanceRepository.getBalance(accountId).orElse(new Balance(0, OffsetDateTime.now()));
    balanceRepository.saveBalance(accountId, newBalance);

    return newBalance;
  }

  public void saveBalance(UUID accountId, Balance newBalance) {
    balanceRepository.saveBalance(accountId, newBalance);
  }
}
