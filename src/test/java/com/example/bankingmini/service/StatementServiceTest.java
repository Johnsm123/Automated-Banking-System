package com.example.bankingmini.service;

import com.example.bankingmini.dto.AccountDtos;
import com.example.bankingmini.exception.NotFoundException;
import com.example.bankingmini.model.Account;
import com.example.bankingmini.model.Customer;
import com.example.bankingmini.model.TransactionEntity;
import com.example.bankingmini.repository.AccountRepository;
import com.example.bankingmini.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StatementServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private StatementService statementService;

    private Account testAccount;
    private Customer testCustomer;
    private TransactionEntity testTransaction;
    private AccountDtos.StatementRequest statementRequest;
    private final Long userId = 1L;
    private final String accountNumber = "ACC123456789";

    @BeforeEach
    void setUp() {
        testCustomer = Customer.builder()
                .id(userId)
                .email("test@example.com")
                .name("Test User")
                .build();

        testAccount = Account.builder()
                .id(1L)
                .accountNumber(accountNumber)
                .balance(new BigDecimal("1000.00"))
                .customer(testCustomer)
                .status("ACTIVE")
                .createdAt(OffsetDateTime.now())
                .build();

        testTransaction = TransactionEntity.builder()
                .id(1L)
                .account(testAccount)
                .type("DEPOSIT")
                .amount(new BigDecimal("100.00"))
                .occurredAt(Instant.now())
                .build();

        statementRequest = new AccountDtos.StatementRequest(
                accountNumber,
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 1, 31),
                false
        );
    }

    @Test
    void generate_Success_TextFormat() {
        when(accountRepository.findByAccountNumber(accountNumber))
                .thenReturn(Optional.of(testAccount));
        when(transactionRepository.findByAccountAndOccurredAtBeforeOrderByOccurredAtAsc(
                eq(testAccount), any(Instant.class)))
                .thenReturn(List.of());
        when(transactionRepository.findByAccountAndOccurredAtBetweenOrderByOccurredAtAsc(
                eq(testAccount), any(Instant.class), any(Instant.class)))
                .thenReturn(List.of(testTransaction));

        AccountDtos.StatementResponse result = statementService.generate(statementRequest, userId);

        assertNotNull(result);
        assertEquals(accountNumber, result.accountNumber());
        assertEquals("text/plain", result.contentType());
        assertNotNull(result.payload());
        assertTrue(result.payload().contains("Statement for"));
        verify(accountRepository).findByAccountNumber(accountNumber);
    }

    @Test
    void generate_Success_CsvFormat() {
        AccountDtos.StatementRequest csvRequest = new AccountDtos.StatementRequest(
                accountNumber,
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 1, 31),
                true
        );

        when(accountRepository.findByAccountNumber(accountNumber))
                .thenReturn(Optional.of(testAccount));
        when(transactionRepository.findByAccountAndOccurredAtBeforeOrderByOccurredAtAsc(
                eq(testAccount), any(Instant.class)))
                .thenReturn(List.of());
        when(transactionRepository.findByAccountAndOccurredAtBetweenOrderByOccurredAtAsc(
                eq(testAccount), any(Instant.class), any(Instant.class)))
                .thenReturn(List.of(testTransaction));

        AccountDtos.StatementResponse result = statementService.generate(csvRequest, userId);

        assertNotNull(result);
        assertEquals("text/csv", result.contentType());
        assertTrue(result.payload().contains("Account,From,To,Opening,Closing"));
    }

    @Test
    void generate_AccountNotFound() {
        when(accountRepository.findByAccountNumber(accountNumber))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> statementService.generate(statementRequest, userId));
    }

    @Test
    void generate_AccessDenied() {
        when(accountRepository.findByAccountNumber(accountNumber))
                .thenReturn(Optional.of(testAccount));

        assertThrows(IllegalArgumentException.class,
                () -> statementService.generate(statementRequest, 999L));
    }

    @Test
    void generate_InvalidDateRange() {
        AccountDtos.StatementRequest invalidRequest = new AccountDtos.StatementRequest(
                accountNumber,
                LocalDate.of(2024, 1, 31),
                LocalDate.of(2024, 1, 1), // toDate before fromDate
                false
        );

        when(accountRepository.findByAccountNumber(accountNumber))
                .thenReturn(Optional.of(testAccount));

        assertThrows(IllegalArgumentException.class,
                () -> statementService.generate(invalidRequest, userId));
    }

    @Test
    void generate_WithTransferTransactions() {
        TransactionEntity transferOut = TransactionEntity.builder()
                .id(2L)
                .account(testAccount)
                .type("TRANSFER_OUT")
                .amount(new BigDecimal("50.00"))
                .refAccountId(2L)
                .occurredAt(Instant.now())
                .build();

        TransactionEntity transferIn = TransactionEntity.builder()
                .id(3L)
                .account(testAccount)
                .type("TRANSFER_IN")
                .amount(new BigDecimal("75.00"))
                .refAccountId(3L)
                .occurredAt(Instant.now())
                .build();

        when(accountRepository.findByAccountNumber(accountNumber))
                .thenReturn(Optional.of(testAccount));
        when(transactionRepository.findByAccountAndOccurredAtBeforeOrderByOccurredAtAsc(
                eq(testAccount), any(Instant.class)))
                .thenReturn(List.of());
        when(transactionRepository.findByAccountAndOccurredAtBetweenOrderByOccurredAtAsc(
                eq(testAccount), any(Instant.class), any(Instant.class)))
                .thenReturn(List.of(testTransaction, transferOut, transferIn));

        AccountDtos.StatementResponse result = statementService.generate(statementRequest, userId);

        assertNotNull(result);
        assertEquals(3, result.lines().size());
        // Opening: 0, +100 (deposit), -50 (transfer out), +75 (transfer in) = 125
        assertEquals(new BigDecimal("125.00"), result.closingBalance());
    }

    @Test
    void testGenerate_Deprecated() {
        assertThrows(IllegalArgumentException.class,
                () -> statementService.generate(statementRequest));
    }
}