package com.example.bankingmini.service;

import com.example.bankingmini.dto.AccountDtos;
import com.example.bankingmini.exception.NotFoundException;
import com.example.bankingmini.model.Account;
import com.example.bankingmini.model.TransactionEntity;
import com.example.bankingmini.repository.AccountRepository;
import com.example.bankingmini.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.*;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
//automatically generates a constructor for your class with all final fields and fields annotated with @NonNull.
public class AccountQueryService {
    private final AccountRepository accounts;
    private final TransactionRepository txns;

    public java.math.BigDecimal getBalance(String accountNumber, Long userId) {
        var acc = accounts.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new NotFoundException("Account not found: " + accountNumber));
        
        // Verify the account belongs to the authenticated user
        if (!acc.getCustomer().getId().equals(userId)) {
            throw new IllegalArgumentException("Access denied: Account does not belong to user");
        }
        
        return acc.getBalance();
    }

    public java.util.List<AccountDtos.TxnItem> last5(String accountNumber, Long userId) {
        var acc = accounts.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new NotFoundException("Account not found: " + accountNumber));
        
        // Verify the account belongs to the authenticated user
        if (!acc.getCustomer().getId().equals(userId)) {
            throw new IllegalArgumentException("Access denied: Account does not belong to user");
        }
        
        return txns.findTop5ByAccountOrderByOccurredAtDesc(acc).stream()
                .map(t -> new AccountDtos.TxnItem(
                        t.getType(), t.getAmount(), t.getOccurredAt().toString()
                ))
                .collect(Collectors.toList());
    }


    @Deprecated
    public java.math.BigDecimal getBalance(String accountNumber) {
        throw new IllegalArgumentException("Unauthorized access: User ID required");
    }

    @Deprecated
    public java.util.List<AccountDtos.TxnItem> last5(String accountNumber) {
        throw new IllegalArgumentException("Unauthorized access: User ID required");
    }

    @Deprecated
    public List<TransactionEntity> findByDateRange(Account acc, LocalDate from, LocalDate to) {
        throw new IllegalArgumentException("Unauthorized access: User ID required");
    }

    @Deprecated
    public BigDecimal computeOpening(Account acc, LocalDate from) {
        throw new IllegalArgumentException("Unauthorized access: User ID required");
    }
}
