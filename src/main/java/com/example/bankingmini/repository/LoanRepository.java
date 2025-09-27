package com.example.bankingmini.repository;

import com.example.bankingmini.model.Loan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {
    //    List<Loan> findAllByOrderByCreatedAtDesc();
    Page<Loan> findAllByOrderByCreatedAtDesc(Pageable pageable);

    List<Loan> findByStatus(String status);


    List<Loan> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    //    Page<Loan> findByStatusOrderByApplicationDateDesc(String status, Pageable pageable);
    Page<Loan> findByStatusOrderByCreatedAtDesc(String status, Pageable pageable);
//    List<Loan> findByCustomerIdOrderByApplicationDateDesc(Long customerId);

    @Query("SELECT COUNT(l) FROM Loan l WHERE l.account.id = :accountId AND l.status IN ('PENDING', 'APPROVED', 'ACTIVE')")
    long countActiveLoansForAccount(@Param("accountId") Long accountId);


}
