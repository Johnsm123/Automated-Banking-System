package com.example.bankingmini.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentLoanApplicationRequest {
    private Long accountId;
    private BigDecimal loanAmount;
    private BigDecimal interestRate;
    private Integer tenureMonths;
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
    private Integer moratoriumPeriodMonths;
    private String disbursementType;
}
