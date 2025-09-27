package com.example.bankingmini.service;

import com.example.bankingmini.dto.AdminDashboardDto;
import com.example.bankingmini.dto.LoanAnalyticsDto;
import com.example.bankingmini.dto.LoanOfficerDashboardDto;
import com.example.bankingmini.dto.PortfolioSummaryDto;
import com.example.bankingmini.model.Loan;
import com.example.bankingmini.model.StudentLoan;
import com.example.bankingmini.model.VehicleLoan;
import com.example.bankingmini.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanDashboardServiceTest {

    @Mock
    private VehicleLoanRepository vehicleLoanRepository;

    @Mock
    private StudentLoanRepository studentLoanRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private LoanRepository generalLoanRepository;

    @InjectMocks
    private LoanDashboardService loanDashboardService;

    private VehicleLoan testVehicleLoan;
    private StudentLoan testStudentLoan;
    private Loan testGeneralLoan;

    @BeforeEach
    void setUp() {
        testVehicleLoan = VehicleLoan.builder()
                .id(1L)
                .loanAmount(new BigDecimal("500000.00"))
                .outstandingAmount(new BigDecimal("400000.00"))
                .status("ACTIVE")
                .vehicleType("CAR")
                .build();

        testStudentLoan = StudentLoan.builder()
                .id(2L)
                .loanAmount(new BigDecimal("300000.00"))
                .outstandingAmount(new BigDecimal("250000.00"))
                .status("DISBURSED")
                .courseName("Engineering")
                .build();

        testGeneralLoan = Loan.builder()
                .id(3L)
                .principal(new BigDecimal("100000.00"))
                .outstandingAmount(new BigDecimal("80000.00"))
                .status("ACTIVE")
                .type("PERSONAL")
                .build();
    }

    @Test
    void getLoanOfficerDashboard() {
        when(vehicleLoanRepository.findPendingLoansForProcessing())
                .thenReturn(List.of(testVehicleLoan));
        when(studentLoanRepository.findPendingLoansForProcessing())
                .thenReturn(List.of(testStudentLoan));
        when(vehicleLoanRepository.findAll())
                .thenReturn(List.of(testVehicleLoan));
        when(studentLoanRepository.findAll())
                .thenReturn(List.of(testStudentLoan));

        LoanOfficerDashboardDto result = loanDashboardService.getLoanOfficerDashboard(1L);

        assertNotNull(result);
        assertEquals(1, result.getPendingVehicleLoans());
        assertEquals(1, result.getPendingStudentLoans());
        assertEquals(2, result.getTotalPendingReview());
        assertNotNull(result.getTotalDisbursedAmount());
    }

    @Test
    void getAdminDashboard() {
        when(customerRepository.count()).thenReturn(100L);
        when(accountRepository.count()).thenReturn(150L);
        when(vehicleLoanRepository.findAll()).thenReturn(List.of(testVehicleLoan));
        when(studentLoanRepository.findAll()).thenReturn(List.of(testStudentLoan));
        when(generalLoanRepository.findAll()).thenReturn(List.of(testGeneralLoan));

        AdminDashboardDto result = loanDashboardService.getAdminDashboard();

        assertNotNull(result);
        assertEquals(100L, result.getTotalCustomers());
        assertEquals(150L, result.getTotalAccounts());
        assertEquals(1, result.getTotalVehicleLoans());
        assertEquals(1, result.getTotalStudentLoans());
        assertEquals(1, result.getTotalGeneralLoans());
        assertNotNull(result.getTotalLoanPortfolio());
    }

    @Test
    void getLoanAnalytics() {
        LoanAnalyticsDto result = loanDashboardService.getLoanAnalytics("monthly", "VEHICLE");

        assertNotNull(result);
        assertNotNull(result.getMonthlyDisbursements());
        assertNotNull(result.getLoanTypeDistribution());
        assertNotNull(result.getRiskAnalysis());
        assertNotNull(result.getAverageProcessingTime());
        assertNotNull(result.getApprovalRate());
    }

    @Test
    void getPortfolioSummary() {
        when(vehicleLoanRepository.findAll()).thenReturn(List.of(testVehicleLoan));
        when(studentLoanRepository.findAll()).thenReturn(List.of(testStudentLoan));

        PortfolioSummaryDto result = loanDashboardService.getPortfolioSummary();

        assertNotNull(result);
        assertNotNull(result.getTotalPortfolioValue());
        assertEquals(2, result.getActiveLoans());
        assertNotNull(result.getPerformanceMetrics());
        assertNotNull(result.getTopPerformingSegments());
    }
}