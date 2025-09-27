package com.example.bankingmini.repository;

import com.example.bankingmini.model.LoanInstallment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoanInstallmentRepository extends JpaRepository<LoanInstallment, Long> {
    
    List<LoanInstallment> findByLoanIdOrderByDueDateDesc(Long loanId);
    
    int countByLoanId(Long loanId);

}
