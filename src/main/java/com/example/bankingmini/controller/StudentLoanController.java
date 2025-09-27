package com.example.bankingmini.controller;

import com.example.bankingmini.dto.*;
import com.example.bankingmini.model.Customer;
import com.example.bankingmini.security.RoleBasedAccessControl;
import com.example.bankingmini.service.StudentLoanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/student-loans")
public class StudentLoanController {

    @Autowired
    private StudentLoanService studentLoanService;

    @Autowired
    private RoleBasedAccessControl accessControl;

    @PostMapping("/apply")
    public ResponseEntity<StudentLoanDto> applyForLoan(@RequestBody StudentLoanApplicationRequest request) {
        Customer user = accessControl.getCurrentUser();
        StudentLoanDto loan = studentLoanService.applyForLoan(request, user.getId());
        return ResponseEntity.ok(loan);
    }

    @GetMapping("/my-loans")
    public ResponseEntity<List<StudentLoanDto>> getMyLoans() {
        Customer user = accessControl.getCurrentUser();
        List<StudentLoanDto> loans = studentLoanService.getCustomerLoans(user.getId());
        return ResponseEntity.ok(loans);
    }

    @GetMapping("/{loanId}")
    public ResponseEntity<StudentLoanDto> getLoanDetails(@PathVariable("loanId") Long loanId) {
        Customer user = accessControl.getCurrentUser();
        StudentLoanDto loan = studentLoanService.getLoanDetails(loanId, user.getId());
        return ResponseEntity.ok(loan);
    }


    @GetMapping("/pending")
    public ResponseEntity<Page<StudentLoanDto>> getPendingLoans(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        accessControl.requireLoanAccess();
        Pageable pageable = PageRequest.of(page, size);
        Page<StudentLoanDto> loans = studentLoanService.getPendingLoans(pageable);
        return ResponseEntity.ok(loans);
    }


    @PostMapping("/{loanId}/approve")
    public ResponseEntity<String> approveLoan(@PathVariable("loanId") Long loanId) {
        accessControl.requireLoanAccess();
        Customer officer = accessControl.getCurrentUser();
        studentLoanService.approveLoan(loanId, officer.getId());
        return ResponseEntity.ok("Student loan approved successfully");
    }

    @PostMapping("/{loanId}/reject")
    public ResponseEntity<String> rejectLoan(@PathVariable("loanId") Long loanId,
                                             @RequestBody RejectStudentLoanRequest request) {
        accessControl.requireLoanAccess();
        Customer officer = accessControl.getCurrentUser();
        studentLoanService.rejectLoan(loanId, request.getReason(), officer.getId());
        return ResponseEntity.ok("Student loan rejected successfully");
    }

    @PostMapping("/{loanId}/disburse")
    public ResponseEntity<String> disburseLoan(@PathVariable("loanId") Long loanId) {
//    public ResponseEntity<String> disburseLoan(@PathVariable("loanId") Long loanId, @RequestBody DisburseLoanRequest request) {
        accessControl.requireLoanAccess();
        studentLoanService.disburseLoan(loanId);
//        studentLoanService.disburseLoan(loanId, request.getDisbursementAmount());
        return ResponseEntity.ok("Student loan disbursed successfully");
    }

    @PostMapping("/{loanId}/renew")
    public ResponseEntity<String> renewLoan(@PathVariable("loanId") Long loanId,
                                            @RequestBody RenewLoanRequest request) {
        accessControl.requireLoanAccess();
        studentLoanService.renewLoan(loanId, request.getAdditionalAmount(), request.getNewTenure());
        return ResponseEntity.ok("Student loan renewed successfully");
    }


    @PostMapping("/{loanId}/close")
    public ResponseEntity<String> closeLoan(@PathVariable("loanId") Long loanId) {
        Customer user = accessControl.getCurrentUser();
        studentLoanService.closeLoan(loanId, user.getId());
        return ResponseEntity.ok("Student loan closed successfully");
    }


    @GetMapping("/all")
    public ResponseEntity<Page<StudentLoanDto>> getAllLoans(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "status", required = false) String status) {

        accessControl.requireLoanAccess();
        Pageable pageable = PageRequest.of(page, size);
        Page<StudentLoanDto> loans = studentLoanService.getAllLoans(status, pageable);
        return ResponseEntity.ok(loans);
    }


    @GetMapping("/{loanId}/installments")
    public ResponseEntity<List<LoanInstallmentDto>> getLoanInstallments(@PathVariable("loanId") Long loanId) {
        Customer user = accessControl.getCurrentUser();
        List<LoanInstallmentDto> installments = studentLoanService.getLoanInstallments(loanId, user.getId());
        return ResponseEntity.ok(installments);
    }

    @PostMapping("/{loanId}/pay-installment")
    public ResponseEntity<String> payInstallment(@PathVariable("loanId") Long loanId,
                                                 @RequestBody PayInstallmentRequest request) {
        Customer user = accessControl.getCurrentUser();
        studentLoanService.payInstallment(loanId, request.getAmount(), user.getId());
        return ResponseEntity.ok("Installment paid successfully");
    }

}
