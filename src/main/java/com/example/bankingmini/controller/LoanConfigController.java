package com.example.bankingmini.controller;

import com.example.bankingmini.model.LoanConfig;
import com.example.bankingmini.repository.LoanConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/loan-config")
public class LoanConfigController {
    
    @Autowired
    private LoanConfigRepository loanConfigRepository;
    
    @GetMapping
    public ResponseEntity<List<LoanConfig>> getAllLoanConfigs() {
        List<LoanConfig> configs = loanConfigRepository.findByActiveTrue();
        return ResponseEntity.ok(configs);
    }
    
    @GetMapping("/{loanType}")
    public ResponseEntity<LoanConfig> getLoanConfig(@PathVariable String loanType) {
        return loanConfigRepository.findByLoanTypeAndActiveTrue(loanType.toUpperCase())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}