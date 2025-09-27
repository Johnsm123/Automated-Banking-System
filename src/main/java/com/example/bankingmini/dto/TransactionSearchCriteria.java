package com.example.bankingmini.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionSearchCriteria {
    private Long accountId;
    private Instant startDate;
    private Instant endDate;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private String type;
    private String category;
}
