package com.example.bankingmini.service;

import com.example.bankingmini.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionLimitServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransactionLimitService transactionLimitService;

    private final Long accountId = 1L;

    @BeforeEach
    void setUp() {
        when(transactionRepository.sumDailyTransactionsByType(any(), any(), any(), any()))
                .thenReturn(BigDecimal.ZERO);
    }

    @Test
    void validateDepositLimits_Success() {
        BigDecimal amount = new BigDecimal("50000");

        assertDoesNotThrow(() -> transactionLimitService.validateDepositLimits(accountId, amount));
        verify(transactionRepository).sumDailyTransactionsByType(eq(accountId), eq("DEPOSIT"), any(Instant.class), any(Instant.class));
    }

    @Test
    void validateDepositLimits_ExceedsPerTransactionLimit() {
        BigDecimal amount = new BigDecimal("150000"); // Exceeds 1 lakh limit
        // Reset mock to ensure no daily limit check interference
        reset(transactionRepository);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> transactionLimitService.validateDepositLimits(accountId, amount));
        
        assertEquals("Deposit amount exceeds per-transaction limit of ₹100000", exception.getMessage());
    }

    @Test
    void validateDepositLimits_ExceedsDailyLimit() {
        BigDecimal amount = new BigDecimal("50000");
        when(transactionRepository.sumDailyTransactionsByType(any(), any(), any(), any()))
                .thenReturn(new BigDecimal("180000")); // Already deposited 1.8 lakhs today

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> transactionLimitService.validateDepositLimits(accountId, amount));
        
        assertTrue(exception.getMessage().contains("Deposit amount exceeds daily limit of ₹200000"));
    }

    @Test
    void validateWithdrawLimits_Success() {
        BigDecimal amount = new BigDecimal("50000");

        assertDoesNotThrow(() -> transactionLimitService.validateWithdrawLimits(accountId, amount));
        verify(transactionRepository).sumDailyTransactionsByType(eq(accountId), eq("WITHDRAW"), any(Instant.class), any(Instant.class));
    }

    @Test
    void validateWithdrawLimits_ExceedsPerTransactionLimit() {
        BigDecimal amount = new BigDecimal("150000"); // Exceeds 1 lakh limit
        // Reset mock to ensure no daily limit check interference
        reset(transactionRepository);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> transactionLimitService.validateWithdrawLimits(accountId, amount));
        
        assertEquals("Withdrawal amount exceeds per-transaction limit of ₹100000", exception.getMessage());
    }

    @Test
    void validateWithdrawLimits_ExceedsDailyLimit() {
        BigDecimal amount = new BigDecimal("50000");
        when(transactionRepository.sumDailyTransactionsByType(any(), any(), any(), any()))
                .thenReturn(new BigDecimal("180000")); // Already withdrew 1.8 lakhs today

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> transactionLimitService.validateWithdrawLimits(accountId, amount));
        
        assertTrue(exception.getMessage().contains("Withdrawal amount exceeds daily limit of ₹200000"));
    }

    @Test
    void validateTransferLimits_Success() {
        BigDecimal amount = new BigDecimal("50000");

        assertDoesNotThrow(() -> transactionLimitService.validateTransferLimits(accountId, amount));
        verify(transactionRepository).sumDailyTransactionsByType(eq(accountId), eq("TRANSFER_OUT"), any(Instant.class), any(Instant.class));
    }

    @Test
    void validateTransferLimits_ExceedsPerTransactionLimit() {
        BigDecimal amount = new BigDecimal("150000"); // Exceeds 1 lakh limit
        // Reset mock to ensure no daily limit check interference
        reset(transactionRepository);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> transactionLimitService.validateTransferLimits(accountId, amount));
        
        assertEquals("Transfer amount exceeds per-transaction limit of ₹100000", exception.getMessage());
    }

    @Test
    void validateTransferLimits_ExceedsDailyLimit() {
        BigDecimal amount = new BigDecimal("50000");
        when(transactionRepository.sumDailyTransactionsByType(any(), any(), any(), any()))
                .thenReturn(new BigDecimal("180000")); // Already transferred 1.8 lakhs today

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> transactionLimitService.validateTransferLimits(accountId, amount));
        
        assertTrue(exception.getMessage().contains("Transfer amount exceeds daily limit of ₹200000"));
    }

    @Test
    void validateLimits_EdgeCases() {
        // Test exact limit amounts
        BigDecimal exactPerTransactionLimit = new BigDecimal("100000");
        BigDecimal exactDailyLimit = new BigDecimal("100000");
        
        when(transactionRepository.sumDailyTransactionsByType(any(), any(), any(), any()))
                .thenReturn(exactDailyLimit);

        assertDoesNotThrow(() -> transactionLimitService.validateDepositLimits(accountId, exactPerTransactionLimit));
        assertDoesNotThrow(() -> transactionLimitService.validateWithdrawLimits(accountId, exactPerTransactionLimit));
        assertDoesNotThrow(() -> transactionLimitService.validateTransferLimits(accountId, exactPerTransactionLimit));
    }
}