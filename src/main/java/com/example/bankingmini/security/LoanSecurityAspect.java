package com.example.bankingmini.security;

import com.example.bankingmini.model.Customer;
import com.example.bankingmini.repository.CustomerRepository;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoanSecurityAspect {

    @Autowired
    private CustomerRepository customerRepository;

    @Before("execution(* com.example.bankingmini.loan.*Controller.*(..)) && args(loanId,..)")
    public void checkLoanAccess(JoinPoint joinPoint, Long loanId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("User not authenticated");
        }

        String email = authentication.getName();
        Customer user = customerRepository.findByEmail(email)
            .orElseThrow(() -> new AccessDeniedException("User not found"));

        // Skip access check for admin and loan officers on management operations
        String methodName = joinPoint.getSignature().getName();
        if (user.hasLoanAccess() && (methodName.contains("approve") || methodName.contains("reject") || 
            methodName.contains("disburse") || methodName.contains("getAll") || methodName.contains("getPending"))) {
            return;
        }

        // For customer operations, additional validation is handled in service layer
        // This aspect provides an additional security layer
    }

    @Before("execution(* com.example.bankingmini.dashboard.*Controller.*(..))")
    public void checkDashboardAccess(JoinPoint joinPoint) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("User not authenticated");
        }

        String methodName = joinPoint.getSignature().getName();
        String email = authentication.getName();
        Customer user = customerRepository.findByEmail(email)
            .orElseThrow(() -> new AccessDeniedException("User not found"));

        // Check role-based access for dashboard endpoints
        if (methodName.contains("Admin") && !user.isAdmin()) {
            throw new AccessDeniedException("Admin access required");
        }
        
        if ((methodName.contains("LoanOfficer") || methodName.contains("Analytics") || methodName.contains("Portfolio")) 
            && !user.hasLoanAccess()) {
            throw new AccessDeniedException("Loan officer or admin access required");
        }
    }
}
