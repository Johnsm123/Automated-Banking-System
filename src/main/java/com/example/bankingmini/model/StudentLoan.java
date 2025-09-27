package com.example.bankingmini.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "STUDENT_LOAN")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentLoan {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "student_loan_seq")
    @SequenceGenerator(name = "student_loan_seq", sequenceName = "STUDENT_LOAN_SEQ", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "CUSTOMER_ID")
    private Customer customer;

    @ManyToOne(optional = false)
    @JoinColumn(name = "ACCOUNT_ID")
    private Account account;

    @Column(name = "LOAN_AMOUNT", nullable = false)
//    private BigDecimal principal;
    private BigDecimal loanAmount;

    @Column(name = "INTEREST_RATE", nullable = false)
    private BigDecimal interestRate;

    @Column(name = "TENURE_MONTHS", nullable = false)
    private Integer tenureMonths;

    @Column(name = "MONTHLY_EMI")
    private BigDecimal monthlyEmi;

    @Column(name = "COURSE_NAME", nullable = false)
    private String courseName;

    @Column(name = "INSTITUTION_NAME", nullable = false)
    private String institutionName;

    @Column(name = "COURSE_DURATION_YEARS", nullable = false)
    private Integer courseDurationYears;

    @Column(name = "COURSE_FEE", nullable = false)
    private BigDecimal courseFee;

    @Column(name = "ACADEMIC_YEAR", nullable = false)
    private String academicYear;

    @Column(name = "STUDENT_NAME", nullable = false)
    private String studentName;

    @Column(name = "STUDENT_AGE", nullable = false)
    private Integer studentAge;

    @Column(name = "GUARDIAN_NAME")
    private String guardianName;

    @Column(name = "GUARDIAN_INCOME")
    private BigDecimal guardianIncome;

    @Column(name = "COLLATERAL_PROVIDED")
    private Boolean collateralProvided;

    @Column(name = "COLLATERAL_DETAILS")
    private String collateralDetails;

    @Column(name = "STATUS", nullable = false)
    @Builder.Default
    private String status = "PENDING"; // PENDING, APPROVED, REJECTED, DISBURSED, ACTIVE, CLOSED

    @Column(name = "MORATORIUM_PERIOD_MONTHS")
    private Integer moratoriumPeriodMonths; // Grace period after course completion

    @Column(name = "APPLICATION_DATE", nullable = false)
    private Instant applicationDate;

    @Column(name = "APPROVAL_DATE")
    private Instant approvalDate;

    @Column(name = "DISBURSEMENT_DATE")
    private Instant disbursementDate;

    @Column(name = "COURSE_COMPLETION_DATE")
    private Instant courseCompletionDate;

    @Column(name = "EMI_START_DATE")
    private Instant emiStartDate;

    @Column(name = "OUTSTANDING_AMOUNT")
    private BigDecimal outstandingAmount;

    @Column(name = "APPROVED_BY")
    private Long approvedBy;

    @Column(name = "REJECTION_REASON")
    private String rejectionReason;

    @Column(name = "DISBURSEMENT_TYPE")
    @Builder.Default
    private String disbursementType = "SEMESTER_WISE"; // LUMP_SUM, SEMESTER_WISE, YEARLY

    @Column(name = "NEXT_DISBURSEMENT_AMOUNT")
    private BigDecimal nextDisbursementAmount;

    @Column(name = "NEXT_DISBURSEMENT_DATE")
    private Instant nextDisbursementDate;

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
