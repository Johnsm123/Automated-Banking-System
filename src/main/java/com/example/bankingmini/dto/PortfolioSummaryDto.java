package com.example.bankingmini.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioSummaryDto {
    private BigDecimal totalPortfolioValue;
    private Integer activeLoans;
    private Map<String, BigDecimal> performanceMetrics;
    private List<SegmentPerformanceDto> topPerformingSegments;
}
