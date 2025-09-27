package com.example.bankingmini.security;

import com.example.bankingmini.model.Customer;
import com.example.bankingmini.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class RoleBasedAccessControl {

    @Autowired
    private CustomerRepository customerRepository;

    public Customer getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("User not authenticated");
        }

        String email = authentication.getName();
        return customerRepository.findByEmail(email)
                .orElseThrow(() -> new AccessDeniedException("User not found"));
    }
//    public Customer getCurrentUser() {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        if (authentication == null || !authentication.isAuthenticated()) {
//            throw new AuthenticationCredentialsNotFoundException("Authentication required");
//        }
//
//        String email = authentication.getName();
//        return customerRepository.findByEmail(email)
//                .orElseThrow(() -> new AccessDeniedException("User not found"));
//    }


    public void requireAdmin() {
        Customer user = getCurrentUser();
        if (!user.isAdmin()) {
            throw new AccessDeniedException("Admin access required");
        }
    }

    public void requireLoanAccess() {
        Customer user = getCurrentUser();
        if (!user.hasLoanAccess()) {
            throw new AccessDeniedException("Loan officer or admin access required");
        }
    }

//    public void requireUserOrAdmin(Long userId) {
//        Customer currentUser = getCurrentUser();
//        if (!currentUser.isAdmin() && !currentUser.getId().equals(userId)) {
//            throw new AccessDeniedException("Access denied: can only access own data");
//        }
//    }
}
