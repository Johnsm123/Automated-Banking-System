package com.example.bankingmini.service;

import com.example.bankingmini.dto.ChangePasswordRequest;
import com.example.bankingmini.dto.ProfileDto;
import com.example.bankingmini.dto.UpdateProfileRequest;
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
class ProfileServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private ProfileService profileService;

    private Customer testCustomer;
    private final Long userId = 1L;
    private final String email = "test@example.com";
    private final String name = "Test User";
    private final String phone = "1234567890";
    private final String address = "123 Test St";
    private final String dateOfBirth = "1990-01-01";
    private final String passwordHash = "hashedPassword";

    @BeforeEach
    void setUp() {
        testCustomer = Customer.builder()
                .id(userId)
                .email(email)
                .name(name)
                .phone(phone)
                .address(address)
                .dateOfBirth(dateOfBirth)
                .passwordHash(passwordHash)
                .role("USER")
                .createdAt(Instant.now())
                .build();
    }

    @Test
    void getProfile_Success() {
        when(customerRepository.findById(userId)).thenReturn(Optional.of(testCustomer));

        ProfileDto result = profileService.getProfile(userId);

        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertEquals(email, result.getEmail());
        assertEquals(name, result.getName());
        assertEquals(phone, result.getPhone());
        assertEquals(address, result.getAddress());
        assertEquals(dateOfBirth, result.getDateOfBirth());
        assertEquals("USER", result.getRole());
        verify(customerRepository).findById(userId);
    }

    @Test
    void getProfile_CustomerNotFound() {
        when(customerRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> profileService.getProfile(userId));
    }

    @Test
    void updateProfile_Success() {
        UpdateProfileRequest request = new UpdateProfileRequest(
                "Updated Name", "9876543210", "456 New St", "1985-05-15");

        when(customerRepository.findById(userId)).thenReturn(Optional.of(testCustomer));
        when(customerRepository.save(any(Customer.class))).thenReturn(testCustomer);

        ProfileDto result = profileService.updateProfile(userId, request);

        assertNotNull(result);
        assertEquals("Updated Name", testCustomer.getName());
        assertEquals("9876543210", testCustomer.getPhone());
        assertEquals("456 New St", testCustomer.getAddress());
        assertEquals("1985-05-15", testCustomer.getDateOfBirth());
        verify(customerRepository).save(testCustomer);
    }

    @Test
    void updateProfile_PartialUpdate() {
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setName("Updated Name Only");

        when(customerRepository.findById(userId)).thenReturn(Optional.of(testCustomer));
        when(customerRepository.save(any(Customer.class))).thenReturn(testCustomer);

        ProfileDto result = profileService.updateProfile(userId, request);

        assertNotNull(result);
        assertEquals("Updated Name Only", testCustomer.getName());
        assertEquals(phone, testCustomer.getPhone()); // Should remain unchanged
        verify(customerRepository).save(testCustomer);
    }

    @Test
    void updateProfile_CustomerNotFound() {
        UpdateProfileRequest request = new UpdateProfileRequest("New Name", null, null, null);
        when(customerRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> profileService.updateProfile(userId, request));
    }

    @Test
    void changePassword_Success() {
        ChangePasswordRequest request = new ChangePasswordRequest("currentPassword", "newPassword123");
        String newPasswordHash = "newHashedPassword";

        when(customerRepository.findById(userId)).thenReturn(Optional.of(testCustomer));
        when(passwordEncoder.matches("currentPassword", passwordHash)).thenReturn(true);
        when(passwordEncoder.encode("newPassword123")).thenReturn(newPasswordHash);
        when(customerRepository.save(any(Customer.class))).thenReturn(testCustomer);

        profileService.changePassword(userId, request);

        assertEquals(newPasswordHash, testCustomer.getPasswordHash());
        verify(passwordEncoder).matches("currentPassword", passwordHash);
        verify(passwordEncoder).encode("newPassword123");
        verify(customerRepository).save(testCustomer);
    }

    @Test
    void changePassword_CustomerNotFound() {
        ChangePasswordRequest request = new ChangePasswordRequest("currentPassword", "newPassword123");
        when(customerRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> profileService.changePassword(userId, request));
    }

    @Test
    void changePassword_IncorrectCurrentPassword() {
        ChangePasswordRequest request = new ChangePasswordRequest("wrongPassword", "newPassword123");

        when(customerRepository.findById(userId)).thenReturn(Optional.of(testCustomer));
        when(passwordEncoder.matches("wrongPassword", passwordHash)).thenReturn(false);

        assertThrows(RuntimeException.class,
                () -> profileService.changePassword(userId, request));

        verify(passwordEncoder).matches("wrongPassword", passwordHash);
        verify(passwordEncoder, never()).encode(anyString());
        verify(customerRepository, never()).save(any(Customer.class));
    }
}