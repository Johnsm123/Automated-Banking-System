package com.example.bankingmini.controller;

import com.example.bankingmini.dto.TransactionDto;
import com.example.bankingmini.dto.TransactionSearchCriteria;
import com.example.bankingmini.dto.UpdateCategoryRequest;
import com.example.bankingmini.dto.UpdateDescriptionRequest;
import com.example.bankingmini.model.Customer;
import com.example.bankingmini.security.RoleBasedAccessControl;
import com.example.bankingmini.service.EnhancedTransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;

@RestController
@RequestMapping("/api/transactions")
public class EnhancedTransactionController {

    @Autowired
    private EnhancedTransactionService transactionService;

    @Autowired
    private RoleBasedAccessControl accessControl;

    @GetMapping("/history")
    public ResponseEntity<Page<TransactionDto>> getTransactionHistory(
            @RequestParam(value = "accountId", required = false) Long accountId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {

        Customer user = accessControl.getCurrentUser();//checks if someone is logged in or not
        Pageable pageable = PageRequest.of(page, size);//creates a PageRequest object that implements the Pageable interface
        
        Page<TransactionDto> transactions;
        if (accountId != null) {
            transactions = transactionService.getTransactionHistory(accountId, user.getId(), pageable);
        } else {
            // Get all transactions for the user across all accounts
            transactions = transactionService.getAllUserTransactions(user.getId(), pageable);
        }
        
        return ResponseEntity.ok(transactions);
    }
}
