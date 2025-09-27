package com.example.bankingmini.controller;

import com.example.bankingmini.service.LoanDashboardService;
import com.example.bankingmini.dto.AdminDashboardDto;
import com.example.bankingmini.dto.LoanAnalyticsDto;
import com.example.bankingmini.dto.LoanOfficerDashboardDto;
import com.example.bankingmini.dto.PortfolioSummaryDto;
import com.example.bankingmini.model.Customer;
import com.example.bankingmini.security.RoleBasedAccessControl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
public class LoanDashboardController {

    @Autowired
    private LoanDashboardService dashboardService;

    @Autowired
    private RoleBasedAccessControl accessControl;

//    @GetMapping("/customer-summary")
//    public ResponseEntity<CustomerDashboardDto> getCustomerDashboard() {
//        Customer user = accessControl.getCurrentUser();
//        CustomerDashboardDto dashboard = dashboardService.getCustomerDashboard(user.getId());
//        return ResponseEntity.ok(dashboard);
//    }

    @GetMapping("/loan-officer-summary")
    public ResponseEntity<LoanOfficerDashboardDto> getLoanOfficerDashboard() {
        accessControl.requireLoanAccess();
        Customer officer = accessControl.getCurrentUser();
        LoanOfficerDashboardDto dashboard = dashboardService.getLoanOfficerDashboard(officer.getId());
        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/admin-summary")
    public ResponseEntity<AdminDashboardDto> getAdminDashboard() {
        accessControl.requireAdmin();
        AdminDashboardDto dashboard = dashboardService.getAdminDashboard();
        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/loan-analytics")
    public ResponseEntity<LoanAnalyticsDto> getLoanAnalytics(
            @RequestParam(value = "period", required = false) String period,
            @RequestParam(value = "loanType", required = false) String loanType) {
        accessControl.requireLoanAccess();
        LoanAnalyticsDto analytics = dashboardService.getLoanAnalytics(period, loanType);
        return ResponseEntity.ok(analytics);
    }


    @GetMapping("/portfolio-summary")
    public ResponseEntity<PortfolioSummaryDto> getPortfolioSummary() {
        accessControl.requireLoanAccess();
        PortfolioSummaryDto portfolio = dashboardService.getPortfolioSummary();
        return ResponseEntity.ok(portfolio);
    }
}
