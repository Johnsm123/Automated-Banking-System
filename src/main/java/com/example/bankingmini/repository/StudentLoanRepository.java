package com.example.bankingmini.repository;

import com.example.bankingmini.model.StudentLoan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface StudentLoanRepository extends JpaRepository<StudentLoan, Long> {
    
    List<StudentLoan> findByCustomerIdOrderByApplicationDateDesc(Long customerId);
    
    Page<StudentLoan> findByStatusOrderByApplicationDateDesc(String status, Pageable pageable);
    
    Page<StudentLoan> findAllByOrderByApplicationDateDesc(Pageable pageable);
    
    @Query("SELECT s FROM StudentLoan s WHERE s.status = 'PENDING' ORDER BY s.applicationDate ASC")
    List<StudentLoan> findPendingLoansForProcessing();
    

    @Query("SELECT COUNT(s) FROM StudentLoan s WHERE s.account.id = :accountId AND s.status IN ('PENDING', 'APPROVED', 'DISBURSED', 'ACTIVE')")
    long countActiveLoansForAccount(@Param("accountId") Long accountId);
    

}
