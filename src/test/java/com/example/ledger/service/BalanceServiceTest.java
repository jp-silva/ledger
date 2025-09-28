package com.example.ledger.service;

import com.example.ledger.models.Balance;
import com.example.ledger.repository.BalanceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BalanceServiceTest {

    @Mock
    private BalanceRepository balanceRepository;

    @InjectMocks
    private BalanceService balanceService;

    private UUID testAccountId;
    private Balance testBalance;
    private OffsetDateTime testTime;

    @BeforeEach
    void setUp() {
        testAccountId = UUID.randomUUID();
        testTime = OffsetDateTime.now();
        testBalance = new Balance(1000, testTime);
    }

    @Test
    void getBalance_WhenBalanceExists_ReturnsExistingBalance() {
        // Given
        when(balanceRepository.getBalance(testAccountId)).thenReturn(Optional.of(testBalance));

        // When
        Balance result = balanceService.getBalance(testAccountId);

        // Then
        assertEquals(testBalance, result);
        verify(balanceRepository).getBalance(testAccountId);
        verify(balanceRepository, never()).saveBalance(testAccountId, testBalance);
    }

    @Test
    void getBalance_WhenBalanceDoesNotExist_CreatesNewBalanceWithZeroAmount() {
        // Given
        when(balanceRepository.getBalance(testAccountId)).thenReturn(Optional.empty());
        when(balanceRepository.saveBalance(eq(testAccountId), any())).thenReturn(new Balance(0, OffsetDateTime.now()));

        // When
        Balance result = balanceService.getBalance(testAccountId);

        // Then
        assertNotNull(result);
        assertEquals(0, result.amount());
        assertNotNull(result.updatedAt());
        verify(balanceRepository).getBalance(testAccountId);
        verify(balanceRepository).saveBalance(eq(testAccountId), any(Balance.class));
    }

    @Test
    void saveBalance_CallsRepositorySaveBalance() {
        // Given
        Balance balanceToSave = new Balance(500, testTime);

        // When
        balanceService.saveBalance(testAccountId, balanceToSave);

        // Then
        verify(balanceRepository).saveBalance(testAccountId, balanceToSave);
    }
}
