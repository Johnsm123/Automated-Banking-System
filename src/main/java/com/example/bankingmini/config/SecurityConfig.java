package com.example.bankingmini.config;

import com.example.bankingmini.security.JwtAuthenticationFilter;
import com.example.bankingmini.security.JwtAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers(HttpMethod.POST, "/api/auth/register", "/api/auth/login", "/api/auth/refresh").permitAll()
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-resources/**", "/webjars/**").permitAll()
                        
                        // Static resources and Thymeleaf pages
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()
                        .requestMatchers("/", "/login", "/register", "/dashboard", "/accounts", "/transactions", "/transaction-history", "/loans", "/loan-application", "/my-loans", "/profile", "/loan-management", "/admin").permitAll()

                        // Admin-only endpoints
                        .requestMatchers(HttpMethod.POST, "/api/auth/register-admin").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/auth/register-loan-officer").hasRole("ADMIN") // Added loan officer registration endpoint (admin-only)
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/dashboard/admin-summary").hasRole("ADMIN")

                        // Loan officer and admin endpoints
                        .requestMatchers(HttpMethod.GET, "/api/vehicle-loans/pending", "/api/vehicle-loans/all").hasAnyRole("LOAN_OFFICER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/vehicle-loans/*/approve", "/api/vehicle-loans/*/reject", "/api/vehicle-loans/*/disburse").hasAnyRole("LOAN_OFFICER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/student-loans/pending", "/api/student-loans/all").hasAnyRole("LOAN_OFFICER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/student-loans/*/approve", "/api/student-loans/*/reject", "/api/student-loans/*/disburse", "/api/student-loans/*/renew").hasAnyRole("LOAN_OFFICER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/dashboard/loan-officer-summary", "/api/dashboard/loan-analytics", "/api/dashboard/portfolio-summary").hasAnyRole("LOAN_OFFICER", "ADMIN")

                        // Customer loan application endpoints (authenticated users)
                        .requestMatchers(HttpMethod.POST, "/api/vehicle-loans/apply", "/api/student-loans/apply").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/vehicle-loans/my-loans", "/api/student-loans/my-loans").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/vehicle-loans/*", "/api/student-loans/*").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/student-loans/*/close", "/api/student-loans/*/pay-installment").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/student-loans/*/installments").hasAnyRole("USER", "ADMIN")

                        // Loan officer and admin endpoints
                        .requestMatchers(HttpMethod.GET, "/api/loan/pending", "/api/loan/all").hasAnyRole("LOAN_OFFICER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/loan/*/approve", "/api/loan/*/reject", "/api/loan/*/disburse").hasAnyRole("LOAN_OFFICER", "ADMIN")

                        // Customer loan application endpoints (authenticated users)
                        .requestMatchers(HttpMethod.POST, "/api/loan/request", "/api/loan/apply").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/loan/my-loans", "/api/loan/*", "/api/loan/*/installments").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/loan/*/pay-installment", "/api/loan/*/renew", "/api/loan/*/close").hasAnyRole("USER", "ADMIN")

                        // Profile management endpoints
                        .requestMatchers("/api/profile/**").hasAnyRole("USER", "ADMIN")

                        // Enhanced transaction endpoints
                        .requestMatchers("/api/transactions/**").hasAnyRole("USER", "ADMIN")

                        // Customer dashboard
                        .requestMatchers(HttpMethod.GET, "/api/dashboard/customer-summary").hasAnyRole("USER", "ADMIN")

                        // All other authenticated endpoints
                        .anyRequest().authenticated()
                )
                .httpBasic(httpBasic -> httpBasic.disable())
                .formLogin(form -> form
                        .loginPage("/login")
                        .permitAll()
                        .disable())
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login")
                        .permitAll()
                        .disable())
                .exceptionHandling(ex -> ex.authenticationEntryPoint(jwtAuthenticationEntryPoint))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("http://localhost:*", "https://localhost:*", "http://127.0.0.1:5500"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
