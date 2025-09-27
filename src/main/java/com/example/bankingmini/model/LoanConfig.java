package com.example.bankingmini.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "loan_config")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanConfig {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String loanType; // GENERAL, STUDENT, VEHICLE
    
    @Column(nullable = false)
    private BigDecimal minPrincipal;
    
    @Column(nullable = false)
    private BigDecimal maxPrincipal;
    
    @Column(nullable = false)
    private BigDecimal interestRate;
    
    @Column(nullable = false)
    private Integer minTenureMonths;
    
    @Column(nullable = false)
    private Integer maxTenureMonths;
    
    @Column
    private String description;
    
    @Column(nullable = false)
    private Boolean active = true;
}