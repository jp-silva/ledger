package com.example.ledger.api;

import com.example.ledger.exceptions.InsufficientFundsException;
import com.example.ledger.generated.model.Error;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(InsufficientFundsException.class)
  public ResponseEntity<Error> handleInsufficientFundsException(InsufficientFundsException ex) {
    Error error = new Error();
    error.setMessage(ex.getMessage());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
  }
}
