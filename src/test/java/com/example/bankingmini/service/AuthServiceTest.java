package com.example.bankingmini.service;

import com.example.bankingmini.exception.NotFoundException;
import com.example.bankingmini.model.Customer;
import com.example.bankingmini.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private Customer testCustomer;
    private final String email = "test@example.com";
    private final String password = "Password123";
    private final String name = "Test User";
    private final String phone = "1234567890";
    private final String address = "123 Test St";
    private final String dateOfBirth = "1990-01-01";
    private final String encodedPassword = "encodedPassword123";

    @BeforeEach
    void setUp() {
        testCustomer = Customer.builder()
                .id(1L)
                .email(email)
                .passwordHash(encodedPassword)
                .name(name)
                .phone(phone)
                .address(address)
                .dateOfBirth(dateOfBirth)
                .role("USER")
                .createdAt(Instant.now())
                .build();
    }

    @Test
    void register_Success() {
        when(customerRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(password)).thenReturn(encodedPassword);
        when(customerRepository.save(any(Customer.class))).thenReturn(testCustomer);

        Customer result = authService.register(email, password, name, phone, address, dateOfBirth);

        assertNotNull(result);
        assertEquals(email, result.getEmail());
        assertEquals("USER", result.getRole());
        verify(customerRepository).findByEmail(email);
        verify(passwordEncoder).encode(password);
        verify(customerRepository).save(any(Customer.class));
    }

    @Test
    void register_WeakPassword() {
        String weakPassword = "weak";

        assertThrows(IllegalArgumentException.class,
                () -> authService.register(email, weakPassword, name, phone, address, dateOfBirth));
    }

    @Test
    void register_EmailAlreadyExists() {
        when(customerRepository.findByEmail(email)).thenReturn(Optional.of(testCustomer));

        assertThrows(IllegalArgumentException.class,
                () -> authService.register(email, password, name, phone, address, dateOfBirth));
    }

    @Test
    void registerAdmin_Success() {
        when(customerRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(password)).thenReturn(encodedPassword);
        when(customerRepository.save(any(Customer.class))).thenReturn(testCustomer);

        Customer result = authService.registerAdmin(email, password, name);

        assertNotNull(result);
        verify(customerRepository).save(any(Customer.class));
    }

    @Test
    void registerAdmin_WeakPassword() {
        String weakPassword = "weak";

        assertThrows(IllegalArgumentException.class,
                () -> authService.registerAdmin(email, weakPassword, name));
    }

    @Test
    void register_UnderAge() {
        String underAgeDateOfBirth = "2010-01-01"; // 14 years old

        assertThrows(IllegalArgumentException.class,
                () -> authService.register(email, password, name, phone, address, underAgeDateOfBirth));
    }

    @Test
    void registerLoanOfficer_Success() {
        when(customerRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(password)).thenReturn(encodedPassword);
        when(customerRepository.save(any(Customer.class))).thenReturn(testCustomer);

        Customer result = authService.registerLoanOfficer(email, password, name);

        assertNotNull(result);
        verify(customerRepository).save(any(Customer.class));
    }

    @Test
    void registerLoanOfficer_WeakPassword() {
        String weakPassword = "weak";

        assertThrows(IllegalArgumentException.class,
                () -> authService.registerLoanOfficer(email, weakPassword, name));
    }

    @Test
    void authenticate_Success() {
        when(customerRepository.findByEmail(email)).thenReturn(Optional.of(testCustomer));
        when(passwordEncoder.matches(password, encodedPassword)).thenReturn(true);

        Customer result = authService.authenticate(email, password);

        assertNotNull(result);
        assertEquals(testCustomer, result);
        verify(customerRepository).findByEmail(email);
        verify(passwordEncoder).matches(password, encodedPassword);
    }

    @Test
    void authenticate_UserNotFound() {
        when(customerRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> authService.authenticate(email, password));
    }

    @Test
    void authenticate_InvalidPassword() {
        when(customerRepository.findByEmail(email)).thenReturn(Optional.of(testCustomer));
        when(passwordEncoder.matches(password, encodedPassword)).thenReturn(false);

        assertThrows(IllegalArgumentException.class,
                () -> authService.authenticate(email, password));
    }

    @Test
    void findByEmail_Found() {
        when(customerRepository.findByEmail(email)).thenReturn(Optional.of(testCustomer));

        Customer result = authService.findByEmail(email);

        assertNotNull(result);
        assertEquals(testCustomer, result);
    }

    @Test
    void findByEmail_NotFound() {
        when(customerRepository.findByEmail(email)).thenReturn(Optional.empty());

        Customer result = authService.findByEmail(email);

        assertNull(result);
    }

    @Test
    void findById_Found() {
        Long id = 1L;
        when(customerRepository.findById(id)).thenReturn(Optional.of(testCustomer));

        Customer result = authService.findById(id);

        assertNotNull(result);
        assertEquals(testCustomer, result);
    }

    @Test
    void findById_NotFound() {
        Long id = 1L;
        when(customerRepository.findById(id)).thenReturn(Optional.empty());

        Customer result = authService.findById(id);

        assertNull(result);
    }
}