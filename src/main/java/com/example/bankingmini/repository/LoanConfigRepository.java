package com.example.bankingmini.repository;

import com.example.bankingmini.model.LoanConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LoanConfigRepository extends JpaRepository<LoanConfig, Long> {
    
    Optional<LoanConfig> findByLoanTypeAndActiveTrue(String loanType);
    
    List<LoanConfig> findByActiveTrue();
}