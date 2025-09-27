package com.example.bankingmini.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "LOAN_INSTALLMENT")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanInstallment {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "installment_seq")
    @SequenceGenerator(name = "installment_seq", sequenceName = "INSTALLMENT_SEQ", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    @Column(name = "LOAN_ID", nullable = false)
    private Long loanId;

    @Column(name = "LOAN_TYPE", nullable = false)
    private String loanType; // STUDENT, VEHICLE, GENERAL

    @Column(name = "INSTALLMENT_NUMBER", nullable = false)
    private Integer installmentNumber;

    @Column(name = "AMOUNT", nullable = false)
    private BigDecimal amount;

    @Column(name = "DUE_DATE", nullable = false)
    private Instant dueDate;

    @Column(name = "PAID_DATE")
    private Instant paidDate;

    @Column(name = "PAID_AMOUNT")
    private BigDecimal paidAmount;

    @Column(name = "STATUS", nullable = false)
    @Builder.Default
    private String status = "PENDING"; // PENDING, PAID, OVERDUE, PARTIAL

    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private Instant createdAt;

    public boolean isPending() {
        return "PENDING".equals(this.status);
    }

    public boolean isPaid() {
        return "PAID".equals(this.status);
    }

    public boolean isOverdue() {
        return "OVERDUE".equals(this.status);
    }

    public boolean isPartial() {
        return "PARTIAL".equals(this.status);
    }
}
