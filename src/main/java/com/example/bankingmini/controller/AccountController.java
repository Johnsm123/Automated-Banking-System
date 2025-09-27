package com.example.bankingmini.controller;

import com.example.bankingmini.dto.AccountDtos;
import com.example.bankingmini.dto.AccountDtos.*;
import com.example.bankingmini.service.AccountQueryService;
import com.example.bankingmini.service.AccountService;
import com.example.bankingmini.service.StatementService;
import com.example.bankingmini.repository.CustomerRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService service;
    private final AccountQueryService queries;
    private final StatementService statements;
    private final CustomerRepository customerRepository;

    private Long requireUser() {
        //  Get Authentication object from SecurityContext
        var auth = SecurityContextHolder.getContext().getAuthentication();

        //  Check if the user is authenticated
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            throw new IllegalArgumentException("Not authenticated");
        }
        //  Extract principal
        String email = (String) auth.getPrincipal();

        var customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return customer.getId();
    }


    @PostMapping("/create")
    public CreateAccountResponse createAccount(@Valid @RequestBody CreateAccountRequest req, HttpServletRequest request) {
        Long customerId = requireUser();
//        Long customerId = requireUser(request);
        var account = service.createAccount(customerId, req.accountType());
        return new CreateAccountResponse(
                account.getAccountNumber(),
                req.accountType(),
                account.getBalance(),
                account.getStatus()
        );
    }

    @PostMapping("/deposit")
    public void deposit(@Valid @RequestBody MoneyRequest req, HttpServletRequest request) {
        Long userId = requireUser();
        log.info("Deposit request: {} to account {} by user ID: {}", req.amount(), req.accountNumber(), userId);
        service.deposit(req.accountNumber(), req.amount(), userId);
    }

    @PostMapping("/withdraw")
    public void withdraw(@Valid @RequestBody MoneyRequest req, HttpServletRequest request) {
        Long userId = requireUser();
        log.info("Withdrawal request: {} from account {} by user ID: {}", req.amount(), req.accountNumber(), userId);
        service.withdraw(req.accountNumber(), req.amount(), userId);
    }

    @PostMapping("/transfer")
    public void transfer(@Valid @RequestBody TransferRequest req, HttpServletRequest request) {
        Long userId = requireUser();
        log.info("Transfer request: {} from {} to {} by user ID: {}", req.amount(), req.fromAccount(), req.toAccount(), userId);
        service.transfer(req.fromAccount(), req.toAccount(), req.amount(), userId);
    }

    @GetMapping("/balance")
    public BalanceResponse balance(@RequestParam("accountNumber") String accountNumber, HttpServletRequest request) {
        Long userId = requireUser();
        var bal = queries.getBalance(accountNumber, userId);
        return new BalanceResponse(accountNumber, bal);
    }

    @GetMapping("/mini-statement")
    public MiniStatementResponse mini(@RequestParam("accountNumber") String accountNumber, HttpServletRequest request) {
        Long userId = requireUser();
        var last5 = queries.last5(accountNumber, userId);
        return new MiniStatementResponse(accountNumber, last5);
    }

    @PostMapping("/statement")
    public StatementResponse statement(@Valid @RequestBody
                                               AccountDtos.StatementRequest req, HttpServletRequest request) {
        Long userId = requireUser();
        return statements.generate(req, userId);
    }

    @GetMapping("/list")
    public List<AccountSummaryDto> getCustomerAccounts() {
        Long customerId = requireUser();
        var accounts = service.getCustomerAccounts(customerId);
        return accounts.stream()
                .map(account -> new AccountSummaryDto(
                        account.getId(),
                        account.getAccountNumber(),
                        account.getBalance(),
                        "SAVINGS", // Default account type since it's not stored in Account entity
                        account.getStatus(),
                        account.getCreatedAt().toString()
                ))
                .collect(java.util.stream.Collectors.toList());
    }
}