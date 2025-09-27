package com.example.bankingmini.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

public class LoanDtos {
    public record LoanRequest(
            @NotNull @DecimalMin(value = "0.01") @Digits(integer = 16, fraction = 2) BigDecimal principal,
            @NotBlank String type, // EDUCATION, STUDENT, PERSONAL
            @NotNull @DecimalMin(value = "0.0000") @Digits(integer = 3, fraction = 4) BigDecimal
            interestRate,
            @NotNull @Min(1) @Max(360) Integer tenureMonths
    ) {
    }

    public record LoanResponse(
            Long id,
            String status,
            Instant approvedAt,
            String type,
            BigDecimal principal,
            BigDecimal interestRate,
            Integer tenureMonths
    ) {
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoanDto {
        private Long id;
        private Long customerId;
        private String customerName;
        private String customerEmail;
        private Long accountId;
        private String accountNumber;
        private BigDecimal principal;
        private BigDecimal interestRate;
        private Integer tenureMonths;
        private BigDecimal monthlyEmi;
        private String type;
        private String status;
        private Instant createdAt;
        private Instant approvedAt;
        private Instant disbursementDate;
        private Instant emiStartDate;
        private BigDecimal outstandingAmount;
        private String rejectionReason;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoanApplicationRequest {
        private Long accountId;
        private BigDecimal principal;
        private String type;
        private BigDecimal interestRate;
        private Integer tenureMonths;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RejectGeneralLoanRequest {
        private String reason;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RenewGeneralLoanRequest {
        private BigDecimal additionalAmount;
        private Integer newTenure;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PayGeneralLoanInstallmentRequest {
        private BigDecimal amount;
    }
}
