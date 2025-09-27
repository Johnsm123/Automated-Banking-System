package com.example.bankingmini.config;

import com.example.bankingmini.model.LoanConfig;
import com.example.bankingmini.repository.LoanConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class LoanConfigInitializer implements CommandLineRunner {
    
    @Autowired
    private LoanConfigRepository loanConfigRepository;
    
    @Override
    public void run(String... args) throws Exception {
        // Check if loan configs already exist
        if (loanConfigRepository.count() == 0) {
            initializeLoanConfigs();
        }
    }
    
    private void initializeLoanConfigs() {
        // General Loan
        LoanConfig generalLoan = LoanConfig.builder()
                .loanType("GENERAL")
                .minPrincipal(new BigDecimal("1000.00"))
                .maxPrincipal(new BigDecimal("100000.00"))
                .interestRate(new BigDecimal("8.5"))
                .minTenureMonths(12)
                .maxTenureMonths(60)
                .description("General purpose personal loan with flexible terms")
                .active(true)
                .build();
        
        // Student Loan
        LoanConfig studentLoan = LoanConfig.builder()
                .loanType("STUDENT")
                .minPrincipal(new BigDecimal("5000.00"))
                .maxPrincipal(new BigDecimal("200000.00"))
                .interestRate(new BigDecimal("6.5"))
                .minTenureMonths(12)
                .maxTenureMonths(120)
                .description("Education loan for students with lower interest rates and longer tenure")
                .active(true)
                .build();
        
        // Vehicle Loan
        LoanConfig vehicleLoan = LoanConfig.builder()
                .loanType("VEHICLE")
                .minPrincipal(new BigDecimal("10000.00"))
                .maxPrincipal(new BigDecimal("500000.00"))
                .interestRate(new BigDecimal("7.5"))
                .minTenureMonths(24)
                .maxTenureMonths(84)
                .description("Vehicle loan for cars, bikes, and other vehicles with competitive rates")
                .active(true)
                .build();
        
        loanConfigRepository.save(generalLoan);
        loanConfigRepository.save(studentLoan);
        loanConfigRepository.save(vehicleLoan);
        
        System.out.println("Loan configurations initialized successfully!");
    }
}