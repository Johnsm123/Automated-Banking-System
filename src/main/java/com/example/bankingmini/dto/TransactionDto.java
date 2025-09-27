package com.example.bankingmini.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDto {
    private Long id;
    private Long accountId;
    private String accountNumber;
    private String type;
    private BigDecimal amount;
    private Long refAccountId;
    private String description;
    private String category;
    private Instant occurredAt;
    private Instant timestamp; // Alias for occurredAt for frontend compatibility
}

