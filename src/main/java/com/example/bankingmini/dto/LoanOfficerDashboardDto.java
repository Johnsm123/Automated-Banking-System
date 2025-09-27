package com.example.bankingmini.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanOfficerDashboardDto {
    private Integer pendingVehicleLoans;
    private Integer pendingStudentLoans;
    private Integer totalPendingReview;
    private BigDecimal totalDisbursedAmount;
    private Integer loansProcessedThisMonth;
    private Map<String, Integer> loanStatusDistribution;
}
