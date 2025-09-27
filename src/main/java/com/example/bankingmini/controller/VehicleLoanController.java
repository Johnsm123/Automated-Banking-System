package com.example.bankingmini.controller;

import com.example.bankingmini.dto.*;
import com.example.bankingmini.model.Customer;
import com.example.bankingmini.security.RoleBasedAccessControl;
import com.example.bankingmini.service.VehicleLoanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehicle-loans")
public class VehicleLoanController {

    @Autowired
    private VehicleLoanService vehicleLoanService;

    @Autowired
    private RoleBasedAccessControl accessControl;

    @PostMapping("/apply")
    public ResponseEntity<VehicleLoanDto> applyForLoan(@RequestBody VehicleLoanApplicationRequest request) {
        Customer user = accessControl.getCurrentUser();
        VehicleLoanDto loan = vehicleLoanService.applyForLoan(request, user.getId());
        return ResponseEntity.ok(loan);
    }

    @GetMapping("/my-loans")
    public ResponseEntity<List<VehicleLoanDto>> getMyLoans() {
        Customer user = accessControl.getCurrentUser();
        List<VehicleLoanDto> loans = vehicleLoanService.getCustomerLoans(user.getId());
        return ResponseEntity.ok(loans);
    }

    @GetMapping("/{loanId}")
    public ResponseEntity<VehicleLoanDto> getLoanDetails(@PathVariable("loanId") Long loanId) {
        Customer user = accessControl.getCurrentUser();
        VehicleLoanDto loan = vehicleLoanService.getLoanDetails(loanId, user.getId());
        return ResponseEntity.ok(loan);
    }

    @GetMapping("/{loanId}/installments")
    public ResponseEntity<List<LoanInstallmentDto>> getLoanInstallments(@PathVariable("loanId") Long loanId) {
        Customer user = accessControl.getCurrentUser();
        List<LoanInstallmentDto> installments = vehicleLoanService.getLoanInstallments(loanId, user.getId());
        return ResponseEntity.ok(installments);
    }

    @PostMapping("/{loanId}/pay-installment")
    public ResponseEntity<String> payInstallment(@PathVariable("loanId") Long loanId,
                                                 @RequestBody PayVehicleLoanInstallmentRequest request) {
        Customer user = accessControl.getCurrentUser();
        vehicleLoanService.payInstallment(loanId, request.getAmount(), user.getId());
        return ResponseEntity.ok("Installment paid successfully");
    }

    @PostMapping("/{loanId}/renew")
    public ResponseEntity<String> renewLoan(@PathVariable("loanId") Long loanId,
                                            @RequestBody RenewVehicleLoanRequest request) {
//        Customer user = accessControl.getCurrentUser();
        accessControl.requireLoanAccess();

        vehicleLoanService.renewLoan(loanId, request.getAdditionalAmount(), request.getNewTenure());
        return ResponseEntity.ok("Loan renewed successfully");
    }

    @PostMapping("/{loanId}/close")
    public ResponseEntity<String> closeLoan(@PathVariable("loanId") Long loanId) {
        Customer user = accessControl.getCurrentUser();
        vehicleLoanService.closeLoan(loanId, user.getId());
        return ResponseEntity.ok("Loan closed successfully");
    }

    @GetMapping("/pending")
    public ResponseEntity<Page<VehicleLoanDto>> getPendingLoans(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {

        accessControl.requireLoanAccess();
        Pageable pageable = PageRequest.of(page, size);
        Page<VehicleLoanDto> loans = vehicleLoanService.getPendingLoans(pageable);
        return ResponseEntity.ok(loans);
    }


    @PostMapping("/{loanId}/approve")
    public ResponseEntity<String> approveLoan(@PathVariable("loanId") Long loanId) {
        accessControl.requireLoanAccess();
        Customer officer = accessControl.getCurrentUser();
        vehicleLoanService.approveLoan(loanId, officer.getId());
        return ResponseEntity.ok("Loan approved successfully");
    }


    @PostMapping("/{loanId}/reject")
    public ResponseEntity<String> rejectLoan(@PathVariable("loanId") Long loanId, @RequestBody RejectVehicleLoanRequest request) {
        accessControl.requireLoanAccess();
        Customer officer = accessControl.getCurrentUser();
        vehicleLoanService.rejectLoan(loanId, request.getReason(), officer.getId());
        return ResponseEntity.ok("Loan rejected successfully");
    }

    @PostMapping("/{loanId}/disburse")
    public ResponseEntity<String> disburseLoan(@PathVariable("loanId") Long loanId) {
        accessControl.requireLoanAccess();
        vehicleLoanService.disburseLoan(loanId);
        return ResponseEntity.ok("Loan disbursed successfully");
    }

    @GetMapping("/all")
    public ResponseEntity<Page<VehicleLoanDto>> getAllLoans(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "status", required = false) String status) {
        accessControl.requireLoanAccess();
        Pageable pageable = PageRequest.of(page, size);
        Page<VehicleLoanDto> loans = vehicleLoanService.getAllLoans(status, pageable);
        return ResponseEntity.ok(loans);
    }

}
