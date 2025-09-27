package com.example.bankingmini.service;

import com.example.bankingmini.dto.*;
import com.example.bankingmini.repository.AccountRepository;
import com.example.bankingmini.repository.TransactionRepository;
import com.example.bankingmini.repository.CustomerRepository;
import com.example.bankingmini.repository.LoanRepository;
import com.example.bankingmini.repository.StudentLoanRepository;
import com.example.bankingmini.repository.VehicleLoanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LoanDashboardService {

    @Autowired
    private VehicleLoanRepository vehicleLoanRepository;

    @Autowired
    private StudentLoanRepository studentLoanRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private LoanRepository generalLoanRepository;


    public LoanOfficerDashboardDto getLoanOfficerDashboard(Long officerId) {
        // Get pending loans for review
        var pendingVehicleLoans = vehicleLoanRepository.findPendingLoansForProcessing();
        var pendingStudentLoans = studentLoanRepository.findPendingLoansForProcessing();

        // Get loans processed by this officer in last 30 days
        Instant thirtyDaysAgo = Instant.now().minus(30, ChronoUnit.DAYS);

        // Calculate portfolio metrics
        var allVehicleLoans = vehicleLoanRepository.findAll();
        var allStudentLoans = studentLoanRepository.findAll();

        BigDecimal totalDisbursedAmount = BigDecimal.ZERO;
        totalDisbursedAmount = totalDisbursedAmount.add(allVehicleLoans.stream()
                .filter(loan -> "DISBURSED".equals(loan.getStatus()) || "ACTIVE".equals(loan.getStatus()))
                .map(loan -> loan.getLoanAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add));

        totalDisbursedAmount = totalDisbursedAmount.add(allStudentLoans.stream()
                .filter(loan -> "DISBURSED".equals(loan.getStatus()) || "ACTIVE".equals(loan.getStatus()))
                .map(loan -> loan.getLoanAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add));

        // Loan status distribution
        Map<String, Integer> loanStatusDistribution = new HashMap<>();
        loanStatusDistribution.put("PENDING", pendingVehicleLoans.size() + pendingStudentLoans.size());
        loanStatusDistribution.put("APPROVED", (int) (allVehicleLoans.stream().filter(l -> "APPROVED".equals(l.getStatus())).count() +
                allStudentLoans.stream().filter(l -> "APPROVED".equals(l.getStatus())).count()));
        loanStatusDistribution.put("DISBURSED", (int) (allVehicleLoans.stream().filter(l -> "DISBURSED".equals(l.getStatus())).count() +
                allStudentLoans.stream().filter(l -> "DISBURSED".equals(l.getStatus())).count()));
        loanStatusDistribution.put("ACTIVE", (int) allStudentLoans.stream().filter(l -> "ACTIVE".equals(l.getStatus())).count());

        return LoanOfficerDashboardDto.builder()
                .pendingVehicleLoans(pendingVehicleLoans.size())
                .pendingStudentLoans(pendingStudentLoans.size())
                .totalPendingReview(pendingVehicleLoans.size() + pendingStudentLoans.size())
                .totalDisbursedAmount(totalDisbursedAmount)
                .loansProcessedThisMonth(0) // Would need additional tracking
                .loanStatusDistribution(loanStatusDistribution)
                .build();
    }

    public AdminDashboardDto getAdminDashboard() {
        // Overall system metrics
        long totalCustomers = customerRepository.count();
        long totalAccounts = accountRepository.count();

        var allVehicleLoans = vehicleLoanRepository.findAll();
        var allStudentLoans = studentLoanRepository.findAll();
        var allGeneralLoans = generalLoanRepository.findAll();

        BigDecimal totalLoanPortfolio = BigDecimal.ZERO;
        totalLoanPortfolio = totalLoanPortfolio.add(allVehicleLoans.stream()
                .map(loan -> loan.getLoanAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add));

        totalLoanPortfolio = totalLoanPortfolio.add(allStudentLoans.stream()
                .map(loan -> loan.getLoanAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        totalLoanPortfolio = totalLoanPortfolio.add(allGeneralLoans.stream()
                .map(loan -> loan.getPrincipal())
                .reduce(BigDecimal.ZERO, BigDecimal::add));

        BigDecimal totalOutstanding = BigDecimal.ZERO;
        totalOutstanding = totalOutstanding.add(allVehicleLoans.stream()
                .filter(loan -> loan.getOutstandingAmount() != null)
                .map(loan -> loan.getOutstandingAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add));

        totalOutstanding = totalOutstanding.add(allStudentLoans.stream()
                .filter(loan -> loan.getOutstandingAmount() != null)
                .map(loan -> loan.getOutstandingAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add));

        // Risk metrics
        long overdueLoans =
                allVehicleLoans.stream()
                        .filter(loan -> "ACTIVE".equals(loan.getStatus()))
                        .mapToLong(loan -> 0L)
                        .sum() +
                        allStudentLoans.stream()
                                .filter(loan -> "ACTIVE".equals(loan.getStatus()))
                                .mapToLong(loan -> 0L)
                                .sum() +
                        allGeneralLoans.stream()
                                .filter(loan -> "ACTIVE".equals(loan.getStatus()))
                                .mapToLong(loan -> 0L)
                                .sum();


        return AdminDashboardDto.builder()
                .totalCustomers(totalCustomers)
                .totalAccounts(totalAccounts)
                .totalVehicleLoans(allVehicleLoans.size())
                .totalStudentLoans(allStudentLoans.size())
                .totalGeneralLoans(allGeneralLoans.size())
                .totalLoanPortfolio(totalLoanPortfolio)//will be shown as total amount of laons taken in frontend
                .totalOutstandingAmount(totalOutstanding)
                .overdueLoans(overdueLoans)
//                .collectionEfficiency(BigDecimal.valueOf(95.5)) // Mock data
                .build();
    }

    public LoanAnalyticsDto getLoanAnalytics(String period, String loanType) {
        // Mock analytics data - in real implementation, would calculate from actual data
        Map<String, BigDecimal> monthlyDisbursements = new HashMap<>();
        monthlyDisbursements.put("Jan", BigDecimal.valueOf(500000));
        monthlyDisbursements.put("Feb", BigDecimal.valueOf(750000));
        monthlyDisbursements.put("Mar", BigDecimal.valueOf(600000));
        monthlyDisbursements.put("Apr", BigDecimal.valueOf(800000));

        Map<String, Integer> loanTypeDistribution = new HashMap<>();
        loanTypeDistribution.put("VEHICLE", 45);
        loanTypeDistribution.put("STUDENT", 35);

        Map<String, BigDecimal> riskAnalysis = new HashMap<>();
        riskAnalysis.put("LOW_RISK", BigDecimal.valueOf(60));
        riskAnalysis.put("MEDIUM_RISK", BigDecimal.valueOf(30));
        riskAnalysis.put("HIGH_RISK", BigDecimal.valueOf(10));

        return LoanAnalyticsDto.builder()
                .monthlyDisbursements(monthlyDisbursements)
                .loanTypeDistribution(loanTypeDistribution)
                .riskAnalysis(riskAnalysis)
                .averageProcessingTime(BigDecimal.valueOf(5.2))
                .approvalRate(BigDecimal.valueOf(78.5))
                .build();
    }

    public PortfolioSummaryDto getPortfolioSummary() {
        var allVehicleLoans = vehicleLoanRepository.findAll();
        var allStudentLoans = studentLoanRepository.findAll();

        BigDecimal totalPortfolioValue = BigDecimal.ZERO;
        totalPortfolioValue = totalPortfolioValue.add(allVehicleLoans.stream()
                .map(loan -> loan.getLoanAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add));

        totalPortfolioValue = totalPortfolioValue.add(allStudentLoans.stream()
                .map(loan -> loan.getLoanAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add));

        // Calculate performance metrics
        Map<String, BigDecimal> performanceMetrics = new HashMap<>();
        performanceMetrics.put("TOTAL_DISBURSED", totalPortfolioValue);
        performanceMetrics.put("RECOVERY_RATE", BigDecimal.valueOf(92.3));
        performanceMetrics.put("DEFAULT_RATE", BigDecimal.valueOf(2.1));
        performanceMetrics.put("YIELD", BigDecimal.valueOf(12.5));

        // Top performing segments
        List<SegmentPerformanceDto> topSegments = new ArrayList<>();
        topSegments.add(SegmentPerformanceDto.builder()
                .segmentName("Vehicle Loans")
                .totalAmount(BigDecimal.valueOf(5000000))
                .recoveryRate(BigDecimal.valueOf(94.2))
                .build());
        topSegments.add(SegmentPerformanceDto.builder()
                .segmentName("Student Loans")
                .totalAmount(BigDecimal.valueOf(3000000))
                .recoveryRate(BigDecimal.valueOf(89.8))
                .build());

        return PortfolioSummaryDto.builder()
                .totalPortfolioValue(totalPortfolioValue)
                .activeLoans(allVehicleLoans.size() + allStudentLoans.size())
                .performanceMetrics(performanceMetrics)
                .topPerformingSegments(topSegments)
                .build();
    }
}
