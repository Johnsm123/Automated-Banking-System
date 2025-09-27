package com.example.bankingmini.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "VEHICLE_LOAN")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleLoan {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "vehicle_loan_seq")
    @SequenceGenerator(name = "vehicle_loan_seq", sequenceName = "VEHICLE_LOAN_SEQ", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "CUSTOMER_ID")
    private Customer customer;

    @ManyToOne(optional = false)
    @JoinColumn(name = "ACCOUNT_ID")
    private Account account;

    @Column(name = "LOAN_AMOUNT", nullable = false)
    private BigDecimal loanAmount;

    @Column(name = "INTEREST_RATE", nullable = false)
    private BigDecimal interestRate;

    @Column(name = "TENURE_MONTHS", nullable = false)
    private Integer tenureMonths;

    @Column(name = "MONTHLY_EMI")
    private BigDecimal monthlyEmi;

    @Column(name = "VEHICLE_TYPE", nullable = false)
    private String vehicleType; // CAR, BIKE, TRUCK, etc.

    @Column(name = "VEHICLE_MAKE")
    private String vehicleMake;

    @Column(name = "VEHICLE_MODEL")
    private String vehicleModel;

    @Column(name = "VEHICLE_YEAR")
    private Integer vehicleYear;

    @Column(name = "VEHICLE_PRICE", nullable = false)
    private BigDecimal vehiclePrice;

    @Column(name = "DOWN_PAYMENT")
    private BigDecimal downPayment;

    @Column(name = "STATUS", nullable = false)
    @Builder.Default
    private String status = "PENDING"; // PENDING, APPROVED, REJECTED, DISBURSED, ACTIVE, CLOSED

    @Column(name = "APPLICATION_DATE", nullable = false)
    private Instant applicationDate;

    @Column(name = "APPROVAL_DATE")
    private Instant approvalDate;

    @Column(name = "DISBURSEMENT_DATE")
    private Instant disbursementDate;

    @Column(name = "EMI_START_DATE")
    private Instant emiStartDate;

    @Column(name = "OUTSTANDING_AMOUNT")
    private BigDecimal outstandingAmount;

    @Column(name = "APPROVED_BY")
    private Long approvedBy;

    @Column(name = "REJECTION_REASON")
    private String rejectionReason;

    @Column(name = "INCOME_PROOF")
    private String incomeProof;

    @Column(name = "EMPLOYMENT_TYPE")
    private String employmentType; // SALARIED, SELF_EMPLOYED, BUSINESS

    @Column(name = "MONTHLY_INCOME", nullable = false)
    private BigDecimal monthlyIncome;

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
