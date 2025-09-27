package com.example.bankingmini.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "LOAN")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "loan_seq")
    @SequenceGenerator(
            name = "loan_seq",
            sequenceName = "loan_sequence", // will create/use this sequence in Oracle
            allocationSize = 1              // prevents ID skipping
    )
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "CUSTOMER_ID")
    private Customer customer;

    @ManyToOne(optional = false)
    @JoinColumn(name = "ACCOUNT_ID")
    private Account account;

    @Column(name = "PRINCIPAL", nullable = false)
    private BigDecimal principal;

    @Column(name = "STATUS", nullable = false)
    @Builder.Default
    private String status = "PENDING"; // PENDING, APPROVED, REJECTED, DISBURSED, ACTIVE, CLOSED

    @Column(name = "CREATED_AT", nullable = false)
    private Instant createdAt;

    @Column(name = "APPROVED_AT")
    private Instant approvedAt;

    @Column(name = "DISBURSEMENT_DATE")
    private Instant disbursementDate;

    @Column(name = "EMI_START_DATE")
    private Instant emiStartDate;

    @Column(name = "TYPE", nullable = false)
    private String type; // EDUCATION, STUDENT, PERSONAL

    @Column(name = "INTEREST_RATE", nullable = false)
    private BigDecimal interestRate; // e.g., 0.1200 = 12% APR

    @Column(name = "TENURE_MONTHS", nullable = false)
    private Integer tenureMonths;

    @Column(name = "MONTHLY_EMI")
    private BigDecimal monthlyEmi;

    @Column(name = "OUTSTANDING_AMOUNT")
    private BigDecimal outstandingAmount;

    @Column(name = "APPROVED_BY")
    private Long approvedBy;

    @Column(name = "REJECTION_REASON")
    private String rejectionReason;

    public boolean isPending() {
        return "PENDING".equals(this.status);
    }

    public boolean isApproved() {
        return "APPROVED".equals(this.status);
    }

    public boolean isDisbursed() {
        return "DISBURSED".equals(this.status);
    }

    public boolean isActive() {
        return "ACTIVE".equals(this.status);
    }

    public boolean isClosed() {
        return "CLOSED".equals(this.status);
    }
}
