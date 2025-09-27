package com.example.bankingmini.service;

import com.example.bankingmini.repository.AccountRepository;
import com.example.bankingmini.repository.CustomerRepository;
import com.example.bankingmini.exception.InsufficientFundsException;
import com.example.bankingmini.exception.NotFoundException;
import com.example.bankingmini.model.Account;
import com.example.bankingmini.model.TransactionEntity;
import com.example.bankingmini.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accounts;
    private final TransactionRepository txns;
    private final CustomerRepository customers;
    private final TransactionLimitService limitService;

    @Transactional
    public Account createAccount(Long customerId, String accountType) {
        log.info("Creating account for customer ID: {} with type: {}", customerId, accountType);
        try {
            var customer = customers.findById(customerId)
                    .orElseThrow(() -> {
                        log.error("Customer not found with ID: {}", customerId);
                        return new NotFoundException("Customer not found: " + customerId);
                    });

            // Generate unique account number
            String accountNumber;
            do {
                accountNumber = generateAccountNumber();
            } while (accounts.findByAccountNumber(accountNumber).isPresent());

            var account = Account.builder()
                    .id(System.currentTimeMillis() + new Random().nextInt(1_000))
                    .customer(customer)
                    .accountNumber(accountNumber)   // renamed from .number(...)
                    .balance(BigDecimal.ZERO)
                    .status("ACTIVE")
//                .createdAt(OffsetDateTime.from(Instant.now()))
                    .createdAt(Instant.now().atOffset(ZoneOffset.UTC))
                    .build();

            Account savedAccount = accounts.save(account);
            log.info("Successfully created account {} for customer ID: {}", accountNumber, customerId);
            return savedAccount;
        } catch (Exception e) {
            log.error("Error creating account for customer ID {}: {}", customerId, e.getMessage(), e);
            throw e;
        }
    }

    private String generateAccountNumber() {
        return "ACC" + System.currentTimeMillis() + String.format("%03d", new Random().nextInt(1000));
    }

    private Account findAndLockWithAuth(String accountNumber, Long userId) {
        var acc = accounts.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new NotFoundException("Account not found: " + accountNumber));

        // Verify the account belongs to the authenticated user
        if (!acc.getCustomer().getId().equals(userId)) {
            throw new IllegalArgumentException("Access denied: Account does not belong to user");
        }

        return acc;
    }

    private Account findAndLock(String number) {
        var acc = accounts.findByAccountNumber(number)
                .orElseThrow(() -> new NotFoundException("Account not found: " + number));
        // For Oracle, optimistic locking would need @Version; here we rely on TX boundary and row-level locks via updates.
        return acc;
    }

    @Transactional
    public void deposit(String accountNumber, BigDecimal amount, Long userId) {
        log.info("Processing deposit of {} to account {} for user ID: {}", amount, accountNumber, userId);
        try {
            var acc = findAndLockWithAuth(accountNumber, userId);
            
            // Validate transaction limits
            limitService.validateDepositLimits(acc.getId(), amount);
            
            BigDecimal oldBalance = acc.getBalance();
            acc.setBalance(acc.getBalance().add(amount));
            accounts.save(acc);
            txns.save(TransactionEntity.builder()
                    .account(acc)
                    .type("DEPOSIT")
                    .amount(amount)
                    .occurredAt(Instant.now())
                    .build());
            log.info("Successfully deposited {} to account {}. Balance changed from {} to {}", 
                    amount, accountNumber, oldBalance, acc.getBalance());
        } catch (Exception e) {
            log.error("Error processing deposit of {} to account {} for user ID {}: {}", 
                    amount, accountNumber, userId, e.getMessage(), e);
            throw e;
        }
    }

    @Transactional
    public void withdraw(String accountNumber, BigDecimal amount, Long userId) {
        log.info("Processing withdrawal of {} from account {} for user ID: {}", amount, accountNumber, userId);
        try {
            var acc = findAndLockWithAuth(accountNumber, userId);
            
            // Validate transaction limits
            limitService.validateWithdrawLimits(acc.getId(), amount);
            
            BigDecimal oldBalance = acc.getBalance();
            if (acc.getBalance().compareTo(amount) < 0) {
                log.warn("Withdrawal failed for account {}: Insufficient funds. Balance: {}, Requested: {}", 
                        accountNumber, acc.getBalance(), amount);
                throw new InsufficientFundsException("Insufficient funds");
            }
            acc.setBalance(acc.getBalance().subtract(amount));
            accounts.save(acc);
            txns.save(TransactionEntity.builder()
                    .account(acc)
                    .type("WITHDRAW")
                    .amount(amount)
                    .occurredAt(Instant.now())
                    .build());
            log.info("Successfully withdrew {} from account {}. Balance changed from {} to {}", 
                    amount, accountNumber, oldBalance, acc.getBalance());
        } catch (Exception e) {
            log.error("Error processing withdrawal of {} from account {} for user ID {}: {}", 
                    amount, accountNumber, userId, e.getMessage(), e);
            throw e;
        }
    }

    @Transactional
    public void transfer(String from, String to, BigDecimal amount, Long userId) {
        log.info("Processing transfer of {} from account {} to account {} for user ID: {}", amount, from, to, userId);
        try {
            if (from.equals(to)) {
                log.warn("Transfer failed: Cannot transfer to same account {}", from);
                throw new IllegalArgumentException("Cannot transfer to same account");
            }

            var a = findAndLockWithAuth(from, userId);
            // Destination account can belong to any user (for transfers between users)
            var b = findAndLock(to);
            
            // Validate transaction limits
            limitService.validateTransferLimits(a.getId(), amount);

            BigDecimal fromBalance = a.getBalance();
            BigDecimal toBalance = b.getBalance();
            
            if (a.getBalance().compareTo(amount) < 0) {
                log.warn("Transfer failed from account {} to {}: Insufficient funds. Balance: {}, Requested: {}", 
                        from, to, a.getBalance(), amount);
                throw new InsufficientFundsException("Insufficient funds");
            }
            a.setBalance(a.getBalance().subtract(amount));
            b.setBalance(b.getBalance().add(amount));
            accounts.save(a);
            accounts.save(b);
            txns.save(TransactionEntity.builder()
                    .account(a).type("TRANSFER_OUT").amount(amount).refAccountId(b.getId()).occurredAt(Instant.now()).build());
            txns.save(TransactionEntity.builder()
                    .account(b).type("TRANSFER_IN").amount(amount).refAccountId(a.getId()).occurredAt(Instant.now()).build());
            log.info("Successfully transferred {} from account {} (balance: {} -> {}) to account {} (balance: {} -> {})", 
                    amount, from, fromBalance, a.getBalance(), to, toBalance, b.getBalance());
        } catch (Exception e) {
            log.error("Error processing transfer of {} from account {} to account {} for user ID {}: {}", 
                    amount, from, to, userId, e.getMessage(), e);
            throw e;
        }
    }

    @Deprecated
    public void deposit(String accountNumber, BigDecimal amount) {
        throw new IllegalArgumentException("Unauthorized access: User ID required");
    }

    @Deprecated
    public void withdraw(String accountNumber, BigDecimal amount) {
        throw new IllegalArgumentException("Unauthorized access: User ID required");
    }

    @Deprecated
    public void transfer(String from, String to, BigDecimal amount) {
        throw new IllegalArgumentException("Unauthorized access: User ID required");
    }
    public List<Account> getCustomerAccounts(Long customerId) {
        return accounts.findByCustomerId(customerId);
    }
}
