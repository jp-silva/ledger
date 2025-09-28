package com.example.ledger.mappers;

import com.example.ledger.generated.model.BalanceResponse;
import com.example.ledger.generated.model.TransactionResponse;
import com.example.ledger.models.Balance;
import com.example.ledger.models.Deposit;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AccountMapper {
  /*@Mapping(target = "kycStatus", constant = "UNINITIATED")*/
  TransactionResponse toTransactionResponse(Deposit deposit, TransactionResponse.TypeEnum type);

  BalanceResponse toBalanceResponse(Balance balance);
}
