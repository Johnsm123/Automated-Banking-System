package com.example.bankingmini.controller;

import com.example.bankingmini.dto.LoanInstallmentDto;
import com.example.bankingmini.model.Customer;
import com.example.bankingmini.repository.CustomerRepository;
import com.example.bankingmini.security.RoleBasedAccessControl;
import com.example.bankingmini.dto.LoanDtos.*;
import com.example.bankingmini.service.LoanService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/loan")
@RequiredArgsConstructor
public class LoanController {
    private final LoanService service;
    private final CustomerRepository customerRepository;
    @Autowired
    private RoleBasedAccessControl accessControl;

    public Customer getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("User not authenticated");
        }

        String email = authentication.getName();
        return customerRepository.findByEmail(email)
                .orElseThrow(() -> new AccessDeniedException("User not found"));
    }
    //em calculated while applying for laon
    @PostMapping("/apply")
    public ResponseEntity<LoanDto> applyForLoan(@RequestBody LoanApplicationRequest request) {
        Customer user = getCurrentUser();
        LoanDto loan = service.applyForLoan(request, user.getId());
        return ResponseEntity.ok(loan);
    }

    @GetMapping("/my-loans")
    public ResponseEntity<List<LoanDto>> getMyLoans() {
        Customer user = getCurrentUser();
        List<LoanDto> loans = service.getCustomerLoans(user.getId());
        return ResponseEntity.ok(loans);
    }

    @GetMapping("/{loanId}")
    public ResponseEntity<LoanDto> getLoanDetails(@PathVariable("loanId") Long loanId) {
        Customer user = getCurrentUser();
        LoanDto loan = service.getLoanDetails(loanId, user.getId());
        return ResponseEntity.ok(loan);
    }

    @GetMapping("/{loanId}/installments")
    public ResponseEntity<List<LoanInstallmentDto>> getLoanInstallments(@PathVariable("loanId") Long loanId) {
        Customer user = getCurrentUser();
        List<LoanInstallmentDto> installments = service.getLoanInstallments(loanId, user.getId());
        return ResponseEntity.ok(installments);
    }

    @PostMapping("/{loanId}/pay-installment")
    public ResponseEntity<String> payInstallment(@PathVariable("loanId") Long loanId, 
                                                @RequestBody PayGeneralLoanInstallmentRequest request) {
        Customer user = getCurrentUser();
        service.payInstallment(loanId, request.getAmount(), user.getId());
        return ResponseEntity.ok("Installment paid successfully");
    }

    @PostMapping("/{loanId}/renew")
    public ResponseEntity<String> renewLoan(@PathVariable("loanId") Long loanId, 
                                           @RequestBody RenewGeneralLoanRequest request) {
//        Customer user = getCurrentUser();
        accessControl.requireLoanAccess();

        service.renewLoan(loanId, request.getAdditionalAmount(), request.getNewTenure());
        return ResponseEntity.ok("Loan renewed successfully");
    }

    @PostMapping("/{loanId}/close")
    public ResponseEntity<String> closeLoan(@PathVariable("loanId") Long loanId) {
        Customer user = getCurrentUser();
        service.closeLoan(loanId, user.getId());
        return ResponseEntity.ok("Loan closed successfully");
    }

    @GetMapping("/pending")
    public ResponseEntity<Page<LoanDto>> getPendingLoans(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {

        Customer user = getCurrentUser();
        if (!user.hasLoanAccess()) {
            throw new AccessDeniedException("Access denied: Insufficient permissions");
        }
        Pageable pageable = PageRequest.of(page, size);
        Page<LoanDto> loans = service.getPendingLoans(pageable);
        return ResponseEntity.ok(loans);
    }

    @PostMapping("/{loanId}/approve")
    public ResponseEntity<String> approveLoan(@PathVariable("loanId") Long loanId) {
        Customer officer = getCurrentUser();
        if (!officer.hasLoanAccess()) {
            throw new AccessDeniedException("Access denied: Insufficient permissions");
        }
        service.approveLoan(loanId, officer.getId());
        return ResponseEntity.ok("Loan approved successfully");
    }

    @PostMapping("/{loanId}/reject")
    public ResponseEntity<String> rejectLoan(@PathVariable("loanId") Long loanId, 
                                            @RequestBody RejectGeneralLoanRequest request) {
        Customer officer = getCurrentUser();
        if (!officer.hasLoanAccess()) {
            throw new AccessDeniedException("Access denied: Insufficient permissions");
        }
        service.rejectLoan(loanId, request.getReason(), officer.getId());
        return ResponseEntity.ok("Loan rejected successfully");
    }

    @PostMapping("/{loanId}/disburse")
    public ResponseEntity<String> disburseLoan(@PathVariable("loanId") Long loanId) {
        Customer officer = getCurrentUser();
        if (!officer.hasLoanAccess()) {
            throw new AccessDeniedException("Access denied: Insufficient permissions");
        }
        service.disburseLoan(loanId);
        return ResponseEntity.ok("Loan disbursed successfully");
    }

    @GetMapping("/all")
    public ResponseEntity<Page<LoanDto>> getAllLoans(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "status", required = false) String status) {
        Customer user = getCurrentUser();
        if (!user.hasLoanAccess()) {
            throw new AccessDeniedException("Access denied: Insufficient permissions");
        }
        Pageable pageable = PageRequest.of(page, size);
        Page<LoanDto> loans = service.getAllLoans(status, pageable);
        return ResponseEntity.ok(loans);
    }
}
