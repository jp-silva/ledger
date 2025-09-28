package com.example.ledger.config;

import com.example.ledger.repository.BalanceRepository;
import com.example.ledger.repository.TransactionRepository;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RepositoryConfiguration {
  @Bean
  BalanceRepository balanceRepository() {
    return new BalanceRepository(new ConcurrentHashMap<>());
  }

  @Bean
  TransactionRepository transactionRepository() {
    return new TransactionRepository(new ArrayList<>());
  }
}
