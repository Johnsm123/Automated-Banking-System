package com.example.bankingmini.service;

import com.example.bankingmini.dto.TransactionDto;
import com.example.bankingmini.dto.TransactionSearchCriteria;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnhancedTransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private EnhancedTransactionService enhancedTransactionService;

    private Account testAccount;
    private Customer testCustomer;
    private TransactionEntity testTransaction;
    private Pageable pageable;
    private final Long accountId = 1L;
    private final Long userId = 1L;
    private final Long transactionId = 1L;

    @BeforeEach
    void setUp() {
        testCustomer = Customer.builder()
                .id(userId)
                .email("test@example.com")
                .name("Test User")
                .build();

        testAccount = Account.builder()
                .id(accountId)
                .accountNumber("ACC123456789")
                .balance(new BigDecimal("1000.00"))
                .customer(testCustomer)
                .status("ACTIVE")
                .createdAt(OffsetDateTime.now())
                .build();

        testTransaction = TransactionEntity.builder()
                .id(transactionId)
                .account(testAccount)
                .type("DEPOSIT")
                .amount(new BigDecimal("100.00"))
                .description("Test transaction")
                .category("INCOME")
                .occurredAt(Instant.now())
                .build();

        pageable = PageRequest.of(0, 10);
    }

    @Test
    void getTransactionHistory_Success() {
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(testAccount));
        when(transactionRepository.findByAccountIdOrderByOccurredAtDesc(accountId, pageable))
                .thenReturn(new PageImpl<>(List.of(testTransaction)));

        Page<TransactionDto> result = enhancedTransactionService.getTransactionHistory(accountId, userId, pageable);

        assertEquals(1, result.getContent().size());
        assertEquals("DEPOSIT", result.getContent().get(0).getType());
        verify(accountRepository).findById(accountId);
        verify(transactionRepository).findByAccountIdOrderByOccurredAtDesc(accountId, pageable);
    }

    @Test
    void getTransactionHistory_AccessDenied() {
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(testAccount));

        assertThrows(RuntimeException.class,
                () -> enhancedTransactionService.getTransactionHistory(accountId, 999L, pageable));
    }

    @Test
    void getAllUserTransactions_Success() {
        when(transactionRepository.findByUserIdOrderByOccurredAtDesc(userId, pageable))
                .thenReturn(new PageImpl<>(List.of(testTransaction)));

        Page<TransactionDto> result = enhancedTransactionService.getAllUserTransactions(userId, pageable);

        assertEquals(1, result.getContent().size());
        verify(transactionRepository).findByUserIdOrderByOccurredAtDesc(userId, pageable);
    }
}