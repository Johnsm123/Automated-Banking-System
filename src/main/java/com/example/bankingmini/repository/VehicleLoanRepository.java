package com.example.bankingmini.repository;

import com.example.bankingmini.model.VehicleLoan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VehicleLoanRepository extends JpaRepository<VehicleLoan, Long> {
    
    List<VehicleLoan> findByCustomerIdOrderByApplicationDateDesc(Long customerId);
    
    Page<VehicleLoan> findByCustomerIdOrderByApplicationDateDesc(Long customerId, Pageable pageable);
    
    Page<VehicleLoan> findByStatusOrderByApplicationDateDesc(String status, Pageable pageable);
    
    Page<VehicleLoan> findAllByOrderByApplicationDateDesc(Pageable pageable);
    
    @Query("SELECT v FROM VehicleLoan v WHERE v.status = 'PENDING' ORDER BY v.applicationDate ASC")
    List<VehicleLoan> findPendingLoansForProcessing();

    
    @Query("SELECT COUNT(v) FROM VehicleLoan v WHERE v.account.id = :accountId AND v.status IN ('PENDING', 'APPROVED', 'DISBURSED', 'ACTIVE')")
    long countActiveLoansForAccount(@Param("accountId") Long accountId);
}
