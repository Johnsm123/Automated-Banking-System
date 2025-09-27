package com.example.bankingmini.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VehicleLoanApplicationRequest {
    private Long accountId;
    private BigDecimal loanAmount;
    private BigDecimal interestRate;
    private Integer tenureMonths;
    private String vehicleType;
    private String vehicleMake;
    private String vehicleModel;
    private Integer vehicleYear;
    private BigDecimal vehiclePrice;
    private BigDecimal downPayment;
    private BigDecimal monthlyIncome;
    private String employmentType;
    private String incomeProof;
}
