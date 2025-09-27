package com.example.bankingmini.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardDto {
    private Long totalCustomers;
    private Long totalAccounts;
    private Integer totalVehicleLoans;
    private Integer totalStudentLoans;
    private Integer totalGeneralLoans;
    private BigDecimal totalLoanPortfolio;
    private BigDecimal totalOutstandingAmount;
    private Long overdueLoans;
//    private BigDecimal collectionEfficiency;
}
