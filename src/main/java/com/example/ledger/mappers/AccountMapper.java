package com.example.ledger.mappers;

import com.example.ledger.generated.model.BalanceResponse;
import com.example.ledger.generated.model.TransactionResponse;
import com.example.ledger.models.Balance;
import com.example.ledger.models.Transaction;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AccountMapper {
  TransactionResponse toTransactionResponse(Transaction transaction);

  BalanceResponse toBalanceResponse(Balance balance);
}
