package com.example.bankingmini.controller;

import com.example.bankingmini.service.AccountService;
import com.example.bankingmini.dto.AuthDtos.*;
import com.example.bankingmini.service.AuthService;
import com.example.bankingmini.dto.JwtAuthDtos.*;
import com.example.bankingmini.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;
    private final AccountService accountService;

    @Value("${jwt.expiration:86400000}")
    private long jwtExpirationMs;

    @PostMapping("/register")
    public JwtAuthResponse register(@Valid @RequestBody RegisterRequest req) {
        var customer = authService.register(req.email(), req.password(), req.name(), req.phone(), req.address(), req.dateOfBirth());
        
        // Create default savings account for new user
        accountService.createAccount(customer.getId(), "SAVINGS");

        String accessToken = jwtUtil.generateAccessToken(customer.getId(), customer.getEmail(), customer.getRole());
        String refreshToken = jwtUtil.generateRefreshToken(customer.getId(), customer.getEmail());

        UserInfo userInfo = new UserInfo(customer.getId(), customer.getEmail(), customer.getName(), customer.getRole(), customer.getDateOfBirth());

        return new JwtAuthResponse(accessToken, refreshToken, jwtExpirationMs / 1000, userInfo);
    }

    @PostMapping("/register-admin")
    public JwtAuthResponse registerAdmin(@Valid @RequestBody RegisterRequest req) {
        var customer = authService.registerAdmin(req.email(), req.password(), req.name());

        String accessToken = jwtUtil.generateAccessToken(customer.getId(), customer.getEmail(), customer.getRole());
        String refreshToken = jwtUtil.generateRefreshToken(customer.getId(), customer.getEmail());

        UserInfo userInfo = new UserInfo(customer.getId(), customer.getEmail(), customer.getName(), customer.getRole(), customer.getDateOfBirth());

        return new JwtAuthResponse(accessToken, refreshToken, jwtExpirationMs / 1000, userInfo);
    }

    @PostMapping("/register-loan-officer")
    public JwtAuthResponse registerLoanOfficer(@Valid @RequestBody RegisterRequest req) {
        var customer = authService.registerLoanOfficer(req.email(), req.password(), req.name());

        String accessToken = jwtUtil.generateAccessToken(customer.getId(), customer.getEmail(), customer.getRole());
        String refreshToken = jwtUtil.generateRefreshToken(customer.getId(), customer.getEmail());

        UserInfo userInfo = new UserInfo(customer.getId(), customer.getEmail(), customer.getName(), customer.getRole(), customer.getDateOfBirth());

        return new JwtAuthResponse(accessToken, refreshToken, jwtExpirationMs / 1000, userInfo);
    }

    @PostMapping("/login")
    public JwtAuthResponse login(@Valid @RequestBody LoginRequest req) {
        log.info("Login attempt for email: {}", req.email());
        try {
            var customer = authService.authenticate(req.email(), req.password());

            String accessToken = jwtUtil.generateAccessToken(customer.getId(), customer.getEmail(), customer.getRole());
            String refreshToken = jwtUtil.generateRefreshToken(customer.getId(), customer.getEmail());

            UserInfo userInfo = new UserInfo(customer.getId(), customer.getEmail(), customer.getName(), customer.getRole(), customer.getDateOfBirth());
            
            log.info("Successful login for user ID: {} with role: {}", customer.getId(), customer.getRole());
            return new JwtAuthResponse(accessToken, refreshToken, jwtExpirationMs / 1000, userInfo);
        } catch (Exception e) {
            log.warn("Login failed for email {}: {}", req.email(), e.getMessage());
            throw e;
        }
    }

    @PostMapping("/refresh")
    public RefreshTokenResponse refresh(@Valid @RequestBody RefreshTokenRequest req) {
        String refreshToken = req.refreshToken();

        if (!jwtUtil.isTokenValid(refreshToken) || !jwtUtil.isRefreshToken(refreshToken)) {
            throw new IllegalArgumentException("Invalid refresh token");
        }

        String email = jwtUtil.getEmailFromToken(refreshToken);
        Long userId = jwtUtil.getUserIdFromToken(refreshToken);

        // Verify user still exists
        var customer = authService.findByEmail(email);
        if (customer == null || !customer.getId().equals(userId)) {
            throw new IllegalArgumentException("Invalid refresh token");
        }

        String newAccessToken = jwtUtil.generateAccessToken(customer.getId(), customer.getEmail(), customer.getRole());

        return new RefreshTokenResponse(newAccessToken, jwtExpirationMs / 1000);
    }

    @PostMapping("/logout")
    public void logout(HttpServletRequest request) {
        log.info("Logout request received");
        try {
            // Clear security context
            SecurityContextHolder.clearContext();
            
            // Invalidate session if exists
            if (request.getSession(false) != null) {
                request.getSession().invalidate();
            }
            
            log.info("User successfully logged out");
            // With JWT, logout is primarily handled client-side by removing the token
            // This endpoint ensures server-side cleanup
        } catch (Exception e) {
            log.error("Error during logout: {}", e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/me")
    public UserInfo me() {
        //  Get Authentication object from SecurityContext
        var auth = SecurityContextHolder.getContext().getAuthentication();

        //  Check if user is authenticated
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            throw new IllegalArgumentException("Not authenticated");
        }

        //  Get user email from principal
        String email = (String) auth.getPrincipal();

        //  Fetch customer from authService
        var customer = authService.findByEmail(email);
        if (customer == null) {
            throw new IllegalArgumentException("User not found");
        }

        //  Return user info
        return new UserInfo(customer.getId(), customer.getEmail(), customer.getName(), customer.getRole(), customer.getDateOfBirth());
    }


}
