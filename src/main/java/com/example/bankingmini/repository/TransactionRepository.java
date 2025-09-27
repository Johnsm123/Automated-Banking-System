package com.example.bankingmini.repository;

import com.example.bankingmini.model.Account;
import com.example.bankingmini.model.TransactionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<TransactionEntity, Long> {
    
    List<TransactionEntity> findTop5ByAccountOrderByOccurredAtDesc(Account account);
    List<TransactionEntity> findByAccountAndOccurredAtBetweenOrderByOccurredAtAsc(
            Account account, Instant from, Instant to
    );
    List<TransactionEntity> findByAccountAndOccurredAtBeforeOrderByOccurredAtAsc(
            Account account, Instant before
    );
    
    Page<TransactionEntity> findByAccountIdOrderByOccurredAtDesc(Long accountId, Pageable pageable);

    
    @Query("SELECT t FROM TransactionEntity t WHERE t.account.customer.id = :userId ORDER BY t.occurredAt DESC")
    Page<TransactionEntity> findByUserIdOrderByOccurredAtDesc(@Param("userId") Long userId, Pageable pageable);
    
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM TransactionEntity t WHERE t.account.id = :accountId AND t.type = :type AND t.occurredAt >= :startDate AND t.occurredAt < :endDate")
    BigDecimal sumDailyTransactionsByType(@Param("accountId") Long accountId, 
                                         @Param("type") String type, 
                                         @Param("startDate") Instant startDate, 
                                         @Param("endDate") Instant endDate);
}
