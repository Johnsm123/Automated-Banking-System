package com.example.bankingmini.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanInstallmentDto {
    private Long id;
    private Long loanId;
    private String loanType;
    private Integer installmentNumber;
    private BigDecimal amount;
    private BigDecimal paidAmount;
    private Instant dueDate;
    private Instant paidDate;
    private String status;
}
