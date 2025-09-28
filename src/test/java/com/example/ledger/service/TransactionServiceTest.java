package com.example.ledger.service;

import com.example.ledger.exceptions.InsufficientFundsException;
import com.example.ledger.models.Balance;
import com.example.ledger.models.Transaction;
import com.example.ledger.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private BalanceService balanceService;

    @InjectMocks
    private TransactionService transactionService;

    private UUID testAccountId;
    private Balance testBalance;
    private Transaction testTransaction;
    private OffsetDateTime testTime;

    @BeforeEach
    void setUp() {
        testAccountId = UUID.randomUUID();
        testTime = OffsetDateTime.now();
        testBalance = new Balance(1000, testTime);
        testTransaction = new Transaction(
            UUID.randomUUID(),
            testAccountId,
            500,
            Transaction.TransactionType.DEPOSIT,
            testTime
        );
    }

    @Test
    void createDeposit_WithValidAmount_CreatesDepositAndUpdatesBalance() {
        // Given
        Integer depositAmount = 500;
        when(balanceService.getBalance(testAccountId)).thenReturn(testBalance);
        when(transactionRepository.addTransaction(any(Transaction.class))).thenReturn(testTransaction);

        // When
        Transaction result = transactionService.createDeposit(testAccountId, depositAmount);

        // Then
        assertNotNull(result);
        assertEquals(testAccountId, result.accountId());
        assertEquals(depositAmount, result.amount());
        assertEquals(Transaction.TransactionType.DEPOSIT, result.type());
        assertNotNull(result.id());
        assertNotNull(result.createdAt());

        verify(balanceService).getBalance(testAccountId);
        verify(balanceService).saveBalance(eq(testAccountId), any(Balance.class));
        verify(transactionRepository).addTransaction(any(Transaction.class));

        // Verify that the balance was updated correctly
        verify(balanceService).saveBalance(eq(testAccountId), argThat(balance ->
            balance.amount() == 1500 // original 1000 + deposit 500
        ));
    }

    @Test
    void createWithdrawal_WithSufficientFunds_CreatesWithdrawalAndUpdatesBalance() {
        // Given
        var withdrawalTransaction = new Transaction(
                UUID.randomUUID(),
                testAccountId,
                300,
                Transaction.TransactionType.WITHDRAWAL,
                testTime
        );
        when(balanceService.getBalance(testAccountId)).thenReturn(testBalance);
        when(transactionRepository.addTransaction(any(Transaction.class))).thenReturn(withdrawalTransaction);

        // When
        Transaction result = transactionService.createWithdrawal(testAccountId, withdrawalTransaction.amount());

        // Then
        assertNotNull(result);
        assertEquals(testAccountId, result.accountId());
        assertEquals(withdrawalTransaction.amount(), result.amount());
        assertEquals(Transaction.TransactionType.WITHDRAWAL, result.type());
        assertNotNull(result.id());
        assertNotNull(result.createdAt());

        verify(balanceService).getBalance(testAccountId);
        verify(balanceService).saveBalance(eq(testAccountId), any(Balance.class));
        verify(transactionRepository).addTransaction(any(Transaction.class));

        // Verify that the balance was updated correctly
        verify(balanceService).saveBalance(eq(testAccountId), argThat(balance ->
            balance.amount() == 700 // original 1000 - withdrawal 300
        ));
    }

    @Test
    void createWithdrawal_WithInsufficientFunds_ThrowsIllegalArgumentException() {
        // Given
        Integer withdrawalAmount = 1500; // More than the balance of 1000
        when(balanceService.getBalance(testAccountId)).thenReturn(testBalance);

        // When & Then
        InsufficientFundsException exception = assertThrows(
            InsufficientFundsException.class,
            () -> transactionService.createWithdrawal(testAccountId, withdrawalAmount)
        );

        assertEquals("Balance must be greater than or equal to withdrawal amount", exception.getMessage());
        verify(balanceService).getBalance(testAccountId);
        verify(balanceService, never()).saveBalance(any(UUID.class), any(Balance.class));
        verify(transactionRepository, never()).addTransaction(any(Transaction.class));
    }

    @Test
    void getTransactions_ReturnsTransactionsFromRepository() {
        // Given
        Transaction transaction1 = new Transaction(
            UUID.randomUUID(),
            testAccountId,
            500,
            Transaction.TransactionType.DEPOSIT,
            testTime
        );
        Transaction transaction2 = new Transaction(
            UUID.randomUUID(),
            testAccountId,
            200,
            Transaction.TransactionType.WITHDRAWAL,
            testTime.plusMinutes(10)
        );
        List<Transaction> expectedTransactions = Arrays.asList(transaction1, transaction2);
        when(transactionRepository.getTransactions(testAccountId)).thenReturn(expectedTransactions);

        // When
        List<Transaction> result = transactionService.getTransactions(testAccountId);

        // Then
        assertEquals(expectedTransactions, result);
        assertEquals(2, result.size());
        verify(transactionRepository).getTransactions(testAccountId);
    }

    @Test
    void getTransactions_WhenNoTransactions_ReturnsEmptyList() {
        // Given
        when(transactionRepository.getTransactions(testAccountId)).thenReturn(List.of());

        // When
        List<Transaction> result = transactionService.getTransactions(testAccountId);

        // Then
        assertTrue(result.isEmpty());
        verify(transactionRepository).getTransactions(testAccountId);
    }
}
