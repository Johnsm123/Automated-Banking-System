package com.example.bankingmini.service;

import com.example.bankingmini.dto.TransactionDto;
import com.example.bankingmini.dto.TransactionSearchCriteria;
import com.example.bankingmini.model.Account;
import com.example.bankingmini.model.TransactionEntity;
import com.example.bankingmini.repository.AccountRepository;
import com.example.bankingmini.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class EnhancedTransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AccountRepository accountRepository;

    public Page<TransactionDto> getTransactionHistory(Long accountId, Long userId, Pageable pageable) {
        validateAccountOwnership(accountId, userId);
        Page<TransactionEntity> transactions = transactionRepository.findByAccountIdOrderByOccurredAtDesc(accountId, pageable);
        return transactions.map(this::convertToDto);
    }
    
    public Page<TransactionDto> getAllUserTransactions(Long userId, Pageable pageable) {
        Page<TransactionEntity> transactions = transactionRepository.findByUserIdOrderByOccurredAtDesc(userId, pageable);
        return transactions.map(this::convertToDto);
    }

    private void validateAccountOwnership(Long accountId, Long userId) {
        Account account = accountRepository.findById(accountId)
            .orElseThrow(() -> new RuntimeException("Account not found"));
        
        if (!account.getCustomer().getId().equals(userId)) {
            throw new RuntimeException("Access denied: Account does not belong to user");
        }
    }

    private TransactionDto convertToDto(TransactionEntity transaction) {
        return TransactionDto.builder()
            .id(transaction.getId())
            .accountId(transaction.getAccount().getId())
            .accountNumber(transaction.getAccount().getAccountNumber())
            .type(transaction.getType())
            .amount(transaction.getAmount())
            .refAccountId(transaction.getRefAccountId())
            .description(transaction.getDescription())
            .category(transaction.getCategory())
            .occurredAt(transaction.getOccurredAt())
            .timestamp(transaction.getOccurredAt()) // Add timestamp alias
            .build();
    }
}
