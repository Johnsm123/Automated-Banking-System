package com.example.bankingmini.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentLoanDto {
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
    private String courseName;
    private String institutionName;
    private Integer courseDurationYears;
    private BigDecimal courseFee;
    private String academicYear;
    private String studentName;
    private Integer studentAge;
    private String guardianName;
    private BigDecimal guardianIncome;
    private Boolean collateralProvided;
    private String collateralDetails;
    private String status;
    private Integer moratoriumPeriodMonths;
    private Instant applicationDate;
    private Instant approvalDate;
    private Instant disbursementDate;
    private Instant courseCompletionDate;
    private Instant emiStartDate;
    private BigDecimal outstandingAmount;
    private String disbursementType;
    private BigDecimal nextDisbursementAmount;
    private Instant nextDisbursementDate;
    private String rejectionReason;
}

