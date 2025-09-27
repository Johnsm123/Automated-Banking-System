package com.example.bankingmini.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "TXN")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "txn_seq")
    @SequenceGenerator(name = "txn_seq", sequenceName = "TXN_SEQ", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "ACCOUNT_ID")
    private Account account;

    @Column(name = "TYPE", nullable = false)
    private String type; // DEPOSIT, WITHDRAW, TRANSFER_OUT, TRANSFER_IN

    @Column(name = "AMOUNT", nullable = false)
    private BigDecimal amount;

    @Column(name = "REF_ACCOUNT_ID")
    private Long refAccountId;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "CATEGORY")
    private String category;

    @Column(name = "OCCURRED_AT", nullable = false)
    private Instant occurredAt;
}
