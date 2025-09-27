package com.example.bankingmini.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleLoanDto {
    private Long id;
    private Long customerId;
    private String customerName;
    private String customerEmail;
    private Long accountId;
    private String accountNumber;
    private BigDecimal loanAmount;
    private BigDecimal interestRate;
    private Integer tenureMonths;
    private BigDecimal monthlyEmi;
    private String vehicleType;
    private String vehicleMake;
    private String vehicleModel;
    private Integer vehicleYear;
    private BigDecimal vehiclePrice;
    private BigDecimal downPayment;
    private String status;
    private Instant applicationDate;
    private Instant approvalDate;
    private Instant disbursementDate;
    private Instant emiStartDate;
    private BigDecimal outstandingAmount;
    private BigDecimal monthlyIncome;
    private String employmentType;
    private String rejectionReason;
}

