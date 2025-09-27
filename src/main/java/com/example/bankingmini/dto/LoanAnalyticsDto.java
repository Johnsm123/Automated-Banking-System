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
public class LoanAnalyticsDto {
    private Map<String, BigDecimal> monthlyDisbursements;
    private Map<String, Integer> loanTypeDistribution;
    private Map<String, BigDecimal> riskAnalysis;
    private BigDecimal averageProcessingTime;
    private BigDecimal approvalRate;
}
