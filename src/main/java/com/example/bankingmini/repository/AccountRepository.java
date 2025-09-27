package com.example.bankingmini.repository;

import java.util.List;
import java.util.Optional;

import com.example.bankingmini.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByAccountNumber(String accountNumber);

    List<Account> findByCustomerId(Long customerId);
}

//In databases, a query might not find a matching row.
// Without Optional, you might return null, which can lead to NullPointerException.
//Optional makes it explicit that the result may be empty.
