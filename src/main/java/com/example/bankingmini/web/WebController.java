package com.example.bankingmini.web;

import com.example.bankingmini.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class WebController {
    
    private final JwtUtil jwtUtil;

    @GetMapping("/")
    public String home() {
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "dashboard";
    }

    @GetMapping("/accounts")
    public String accounts() {
        return "accounts";
    }

    @GetMapping("/transactions")
    public String transactions() {
        return "transactions";
    }

    @GetMapping("/transaction-history")
    public String transactionHistory() {
        return "transaction-history";
    }

    @GetMapping("/loans")
    public String loans() {
        return "loans";
    }

    @GetMapping("/loan-application")
    public String loanApplication() {
        return "loan-application";
    }

    @GetMapping("/my-loans")
    public String myLoans() {
        return "my-loans";
    }

    @GetMapping("/profile")
    public String profile() {
        return "profile";
    }

    @GetMapping("/loan-management")
    public String loanManagement() {
        return "loan-management";
    }

    @GetMapping("/admin")
    public String admin() {
        return "admin";
    }
}