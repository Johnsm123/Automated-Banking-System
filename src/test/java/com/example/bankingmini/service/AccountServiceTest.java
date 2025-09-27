package com.example.bankingmini.service;

import com.example.bankingmini.exception.InsufficientFundsException;
import com.example.bankingmini.exception.NotFoundException;
import com.example.bankingmini.model.Account;
import com.example.bankingmini.model.Customer;
import com.example.bankingmini.model.TransactionEntity;
import com.example.bankingmini.repository.AccountRepository;
import com.example.bankingmini.repository.CustomerRepository;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private TransactionLimitService transactionLimitService;

    @InjectMocks
    private AccountService accountService;

    private Customer testCustomer;
    private Account testAccount;
    private Account testAccount2;
    private final Long customerId = 1L;
    private final Long userId = 1L;
    private final String accountNumber = "ACC123456789";
    private final String accountNumber2 = "ACC987654321";

    @BeforeEach
    void setUp() {
        testCustomer = Customer.builder()
                .id(customerId)
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

        testAccount2 = Account.builder()
                .id(2L)
                .accountNumber(accountNumber2)
                .balance(new BigDecimal("500.00"))
                .customer(testCustomer)
                .status("ACTIVE")
                .createdAt(OffsetDateTime.now())
                .build();
    }

    @Test
    void createAccount_Success() {
        when(customerRepository.findById(customerId))
                .thenReturn(Optional.of(testCustomer));
        when(accountRepository.findByAccountNumber(anyString()))
                .thenReturn(Optional.empty());
        when(accountRepository.save(any(Account.class)))
                .thenReturn(testAccount);

        Account result = accountService.createAccount(customerId, "SAVINGS");

        assertNotNull(result);
        verify(customerRepository).findById(customerId);
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void createAccount_CustomerNotFound() {
        when(customerRepository.findById(customerId))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> accountService.createAccount(customerId, "SAVINGS"));
    }

    @Test
    void deposit_Success() {
        BigDecimal amount = new BigDecimal("100.00");
        when(accountRepository.findByAccountNumber(accountNumber))
                .thenReturn(Optional.of(testAccount));
        when(accountRepository.save(any(Account.class)))
                .thenReturn(testAccount);
        when(transactionRepository.save(any(TransactionEntity.class)))
                .thenReturn(new TransactionEntity());
        doNothing().when(transactionLimitService).validateDepositLimits(any(), any());

        accountService.deposit(accountNumber, amount, userId);

        verify(accountRepository).findByAccountNumber(accountNumber);
        verify(accountRepository).save(testAccount);
        verify(transactionRepository).save(any(TransactionEntity.class));
        verify(transactionLimitService).validateDepositLimits(testAccount.getId(), amount);
        assertEquals(new BigDecimal("1100.00"), testAccount.getBalance());
    }

    @Test
    void deposit_AccountNotFound() {
        BigDecimal amount = new BigDecimal("100.00");
        when(accountRepository.findByAccountNumber(accountNumber))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> accountService.deposit(accountNumber, amount, userId));
    }

    @Test
    void deposit_AccessDenied() {
        BigDecimal amount = new BigDecimal("100.00");
        when(accountRepository.findByAccountNumber(accountNumber))
                .thenReturn(Optional.of(testAccount));

        assertThrows(IllegalArgumentException.class,
                () -> accountService.deposit(accountNumber, amount, 999L));
    }

    @Test
    void withdraw_Success() {
        BigDecimal amount = new BigDecimal("100.00");
        when(accountRepository.findByAccountNumber(accountNumber))
                .thenReturn(Optional.of(testAccount));
        when(accountRepository.save(any(Account.class)))
                .thenReturn(testAccount);
        when(transactionRepository.save(any(TransactionEntity.class)))
                .thenReturn(new TransactionEntity());
        doNothing().when(transactionLimitService).validateWithdrawLimits(any(), any());

        accountService.withdraw(accountNumber, amount, userId);

        verify(accountRepository).findByAccountNumber(accountNumber);
        verify(accountRepository).save(testAccount);
        verify(transactionRepository).save(any(TransactionEntity.class));
        verify(transactionLimitService).validateWithdrawLimits(testAccount.getId(), amount);
        assertEquals(new BigDecimal("900.00"), testAccount.getBalance());
    }

    @Test
    void withdraw_InsufficientFunds() {
        BigDecimal amount = new BigDecimal("2000.00");
        when(accountRepository.findByAccountNumber(accountNumber))
                .thenReturn(Optional.of(testAccount));

        assertThrows(InsufficientFundsException.class,
                () -> accountService.withdraw(accountNumber, amount, userId));
    }

    @Test
    void withdraw_AccessDenied() {
        BigDecimal amount = new BigDecimal("100.00");
        when(accountRepository.findByAccountNumber(accountNumber))
                .thenReturn(Optional.of(testAccount));

        assertThrows(IllegalArgumentException.class,
                () -> accountService.withdraw(accountNumber, amount, 999L));
    }

    @Test
    void transfer_Success() {
        BigDecimal amount = new BigDecimal("200.00");
        when(accountRepository.findByAccountNumber(accountNumber))
                .thenReturn(Optional.of(testAccount));
        when(accountRepository.findByAccountNumber(accountNumber2))
                .thenReturn(Optional.of(testAccount2));
        when(accountRepository.save(any(Account.class)))
                .thenReturn(testAccount);
        when(transactionRepository.save(any(TransactionEntity.class)))
                .thenReturn(new TransactionEntity());
        doNothing().when(transactionLimitService).validateTransferLimits(any(), any());

        accountService.transfer(accountNumber, accountNumber2, amount, userId);

        verify(accountRepository).findByAccountNumber(accountNumber);
        verify(accountRepository).findByAccountNumber(accountNumber2);
        verify(accountRepository, times(2)).save(any(Account.class));
        verify(transactionRepository, times(2)).save(any(TransactionEntity.class));
        verify(transactionLimitService).validateTransferLimits(testAccount.getId(), amount);
        assertEquals(new BigDecimal("800.00"), testAccount.getBalance());
        assertEquals(new BigDecimal("700.00"), testAccount2.getBalance());
    }

    @Test
    void transfer_SameAccount() {
        BigDecimal amount = new BigDecimal("100.00");

        assertThrows(IllegalArgumentException.class,
                () -> accountService.transfer(accountNumber, accountNumber, amount, userId));
    }

    @Test
    void transfer_InsufficientFunds() {
        BigDecimal amount = new BigDecimal("2000.00");
        when(accountRepository.findByAccountNumber(accountNumber))
                .thenReturn(Optional.of(testAccount));
        when(accountRepository.findByAccountNumber(accountNumber2))
                .thenReturn(Optional.of(testAccount2));

        assertThrows(InsufficientFundsException.class,
                () -> accountService.transfer(accountNumber, accountNumber2, amount, userId));
    }

    @Test
    void transfer_AccessDenied() {
        BigDecimal amount = new BigDecimal("100.00");
        when(accountRepository.findByAccountNumber(accountNumber))
                .thenReturn(Optional.of(testAccount));

        assertThrows(IllegalArgumentException.class,
                () -> accountService.transfer(accountNumber, accountNumber2, amount, 999L));
    }

    @Test
    void getCustomerAccounts_Success() {
        List<Account> accounts = List.of(testAccount, testAccount2);
        when(accountRepository.findByCustomerId(customerId))
                .thenReturn(accounts);

        List<Account> result = accountService.getCustomerAccounts(customerId);

        assertEquals(2, result.size());
        assertEquals(accounts, result);
        verify(accountRepository).findByCustomerId(customerId);
    }

    @Test
    void testDeposit_Deprecated() {
        BigDecimal amount = new BigDecimal("100.00");

        assertThrows(IllegalArgumentException.class,
                () -> accountService.deposit(accountNumber, amount));
    }

    @Test
    void testWithdraw_Deprecated() {
        BigDecimal amount = new BigDecimal("100.00");

        assertThrows(IllegalArgumentException.class,
                () -> accountService.withdraw(accountNumber, amount));
    }

    @Test
    void testTransfer_Deprecated() {
        BigDecimal amount = new BigDecimal("100.00");

        assertThrows(IllegalArgumentException.class,
                () -> accountService.transfer(accountNumber, accountNumber2, amount));
    }

    @Test
    void deposit_ExceedsLimit() {
        BigDecimal amount = new BigDecimal("150000.00"); // Exceeds 1 lakh limit
        when(accountRepository.findByAccountNumber(accountNumber))
                .thenReturn(Optional.of(testAccount));
        doThrow(new IllegalArgumentException("Deposit amount exceeds per-transaction limit of ₹100000"))
                .when(transactionLimitService).validateDepositLimits(any(), any());

        assertThrows(IllegalArgumentException.class,
                () -> accountService.deposit(accountNumber, amount, userId));
        
        verify(transactionLimitService).validateDepositLimits(testAccount.getId(), amount);
    }

    @Test
    void withdraw_ExceedsLimit() {
        BigDecimal amount = new BigDecimal("150000.00"); // Exceeds 1 lakh limit
        when(accountRepository.findByAccountNumber(accountNumber))
                .thenReturn(Optional.of(testAccount));
        doThrow(new IllegalArgumentException("Withdrawal amount exceeds per-transaction limit of ₹100000"))
                .when(transactionLimitService).validateWithdrawLimits(any(), any());

        assertThrows(IllegalArgumentException.class,
                () -> accountService.withdraw(accountNumber, amount, userId));
        
        verify(transactionLimitService).validateWithdrawLimits(testAccount.getId(), amount);
    }

    @Test
    void transfer_ExceedsLimit() {
        BigDecimal amount = new BigDecimal("150000.00"); // Exceeds 1 lakh limit
        when(accountRepository.findByAccountNumber(accountNumber))
                .thenReturn(Optional.of(testAccount));
        when(accountRepository.findByAccountNumber(accountNumber2))
                .thenReturn(Optional.of(testAccount2));
        doThrow(new IllegalArgumentException("Transfer amount exceeds per-transaction limit of ₹100000"))
                .when(transactionLimitService).validateTransferLimits(any(), any());

        assertThrows(IllegalArgumentException.class,
                () -> accountService.transfer(accountNumber, accountNumber2, amount, userId));
        
        verify(transactionLimitService).validateTransferLimits(testAccount.getId(), amount);
    }
}