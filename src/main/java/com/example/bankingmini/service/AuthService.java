package com.example.bankingmini.service;

import com.example.bankingmini.exception.NotFoundException;
import com.example.bankingmini.model.Customer;
import com.example.bankingmini.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final CustomerRepository customers;
    private final PasswordEncoder passwordEncoder;

    private static final Pattern PWD_PATTERN =
            Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$");

    public Customer register(String email, String password, String name, String phone, String address, String dateOfBirth) {
        log.info("Attempting to register new user with email: {}", email);
        try {
            if (!PWD_PATTERN.matcher(password).matches()) {
                log.warn("Registration failed for email {}: Password does not meet requirements", email);
                throw new IllegalArgumentException("Password must be 8+ chars with upper, lower, digit");
            }
            
            // Age validation - must be 18 or older
            LocalDate birthDate = LocalDate.parse(dateOfBirth);
            int age = Period.between(birthDate, LocalDate.now()).getYears();
            if (age < 18) {
                log.warn("Registration failed for email {}: User is under 18 years old (age: {})", email, age);
                throw new IllegalArgumentException("You must be at least 18 years old to register");
            }
            
            customers.findByEmail(email).ifPresent(c -> {
                log.warn("Registration failed for email {}: Email already registered", email);
                throw new IllegalArgumentException("Email already registered");
            });
            
            var c = Customer.builder()
                    .email(email)
                    .passwordHash(passwordEncoder.encode(password))
                    .name(name)
                    .phone(phone)
                    .address(address)
                    .dateOfBirth(dateOfBirth)
                    .role("USER")
                    .createdAt(Instant.now())
                    .build();
            Customer savedCustomer = customers.save(c);
            log.info("Successfully registered new user with ID: {} and email: {} (age: {})", savedCustomer.getId(), email, age);
            return savedCustomer;
        } catch (Exception e) {
            log.error("Error during user registration for email {}: {}", email, e.getMessage(), e);
            throw e;
        }
    }

    public Customer registerAdmin(String email, String password, String name) {
        if (!PWD_PATTERN.matcher(password).matches()) {
            throw new IllegalArgumentException("Password must be 8+ chars with upper, lower, digit");
        }
        customers.findByEmail(email).ifPresent(c -> {
            throw new IllegalArgumentException("Email already registered");
        });
        var c = Customer.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(password))
                .name(name)
                .role("ADMIN")
                .createdAt(Instant.now())
                .build();
        return customers.save(c);
    }

    public Customer registerLoanOfficer(String email, String password, String name) {
        if (!PWD_PATTERN.matcher(password).matches()) {
            throw new IllegalArgumentException("Password must be 8+ chars with upper, lower, digit");
        }
        customers.findByEmail(email).ifPresent(c -> {
            throw new IllegalArgumentException("Email already registered");
        });
        var c = Customer.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(password))
                .name(name)
                .role("LOAN_OFFICER")
                .createdAt(Instant.now())
                .build();
        return customers.save(c);
    }

    public Customer authenticate(String email, String rawPassword) {
        log.info("Authentication attempt for email: {}", email);
        try {
            var user = customers.findByEmail(email)
                    .orElseThrow(() -> {
                        log.warn("Authentication failed for email {}: User not found", email);
                        return new NotFoundException("User not found");
                    });
            if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
                log.warn("Authentication failed for email {}: Invalid password", email);
                throw new IllegalArgumentException("Invalid credentials");
            }
            log.info("Successfully authenticated user with email: {} and role: {}", email, user.getRole());
            return user;
        } catch (Exception e) {
            log.error("Error during authentication for email {}: {}", email, e.getMessage());
            throw e;
        }
    }

    public Customer findByEmail(String email) {
        return customers.findByEmail(email).orElse(null);
    }

    public Customer findById(Long id) {
        return customers.findById(id).orElse(null);
    }
}
