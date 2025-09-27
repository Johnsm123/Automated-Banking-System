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
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountQueryServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private AccountQueryService accountQueryService;

    private Account testAccount;
    private Customer testCustomer;
    private TransactionEntity testTransaction;
    private final Long userId = 1L;
    private final String accountNumber = "ACC123456789";

    @BeforeEach
    void setUp() {
        
        testCustomer = Customer.builder()
                .id(userId)
                .email("test@example.com")
                .name("Test User")
                .createdAt(Instant.now())
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
    }

    @Test
    void getBalance_Success() {
        when(accountRepository.findByAccountNumber(accountNumber))
                .thenReturn(Optional.of(testAccount));

        BigDecimal balance = accountQueryService.getBalance(accountNumber, userId);

        assertEquals(new BigDecimal("1000.00"), balance);
        verify(accountRepository).findByAccountNumber(accountNumber);
    }

    @Test
    void getBalance_AccountNotFound() {
        when(accountRepository.findByAccountNumber(accountNumber))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, 
                () -> accountQueryService.getBalance(accountNumber, userId));
    }

    @Test
    void getBalance_AccessDenied() {
        when(accountRepository.findByAccountNumber(accountNumber))
                .thenReturn(Optional.of(testAccount));

        assertThrows(IllegalArgumentException.class,
                () -> accountQueryService.getBalance(accountNumber, 999L));
    }

    @Test
    void last5_Success() {
        when(accountRepository.findByAccountNumber(accountNumber))
                .thenReturn(Optional.of(testAccount));
        when(transactionRepository.findTop5ByAccountOrderByOccurredAtDesc(testAccount))
                .thenReturn(List.of(testTransaction));

        List<AccountDtos.TxnItem> transactions = accountQueryService.last5(accountNumber, userId);

        assertEquals(1, transactions.size());
        assertEquals("DEPOSIT", transactions.get(0).type());
        assertEquals(new BigDecimal("100.00"), transactions.get(0).amount());
    }

    @Test
    void last5_AccountNotFound() {
        when(accountRepository.findByAccountNumber(accountNumber))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> accountQueryService.last5(accountNumber, userId));
    }

    @Test
    void last5_AccessDenied() {
        when(accountRepository.findByAccountNumber(accountNumber))
                .thenReturn(Optional.of(testAccount));

        assertThrows(IllegalArgumentException.class,
                () -> accountQueryService.last5(accountNumber, 999L));
    }



    @Test
    void testGetBalance_Deprecated() {
        assertThrows(IllegalArgumentException.class,
                () -> accountQueryService.getBalance(accountNumber));
    }

    @Test
    void testLast5_Deprecated() {
        assertThrows(IllegalArgumentException.class,
                () -> accountQueryService.last5(accountNumber));
    }


}