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
public class SegmentPerformanceDto {
    private String segmentName;
    private BigDecimal totalAmount;
    private BigDecimal recoveryRate;
}
