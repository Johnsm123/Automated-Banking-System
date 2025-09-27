package com.example.bankingmini.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "CUSTOMER")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "customer_seq_gen")
    @SequenceGenerator(name = "customer_seq_gen", sequenceName = "CUSTOMER_SEQ", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    @Column(name = "EMAIL", nullable = false, unique = true)
    private String email;

    @Column(name = "PASSWORD_HASH", nullable = false)
    private String passwordHash;

    @Column(name = "NAME")
    private String name;

    @Column(name = "ROLE", nullable = false)
    @Builder.Default
    private String role = "USER"; // USER, ADMIN, or LOAN_OFFICER

    @Column(name = "PHONE")
    private String phone;

    @Column(name = "ADDRESS")
    private String address;

    @Column(name = "DATE_OF_BIRTH")
    private String dateOfBirth;

    @Column(name = "CREATED_AT", nullable = false)
    private Instant createdAt;

    public boolean isAdmin() {
        return "ADMIN".equals(this.role);
    }

    public boolean isLoanOfficer() {
        return "LOAN_OFFICER".equals(this.role);
    }

    public boolean isUser() {
        return "USER".equals(this.role);
    }

    public boolean hasLoanAccess() {
        return isAdmin() || isLoanOfficer();
    }
}
