package com.example.bankingmini.service;

import com.example.bankingmini.dto.ChangePasswordRequest;
import com.example.bankingmini.dto.ProfileDto;
import com.example.bankingmini.dto.UpdateProfileRequest;
import com.example.bankingmini.model.Customer;
import com.example.bankingmini.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProfileService {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public ProfileDto getProfile(Long userId) {
        Customer customer = customerRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Customer not found"));
        
        return ProfileDto.builder()
            .id(customer.getId())
            .email(customer.getEmail())
            .name(customer.getName())
            .phone(customer.getPhone())
            .address(customer.getAddress())
            .dateOfBirth(customer.getDateOfBirth())
            .role(customer.getRole())
            .createdAt(customer.getCreatedAt())
            .build();
    }

    public ProfileDto updateProfile(Long userId, UpdateProfileRequest request) {
        Customer customer = customerRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Customer not found"));

        if (request.getName() != null) {
            customer.setName(request.getName());
        }
        if (request.getPhone() != null) {
            customer.setPhone(request.getPhone());
        }
        if (request.getAddress() != null) {
            customer.setAddress(request.getAddress());
        }
        if (request.getDateOfBirth() != null) {
            customer.setDateOfBirth(request.getDateOfBirth());
        }

        Customer savedCustomer = customerRepository.save(customer);
        return getProfile(savedCustomer.getId());
    }

    public void changePassword(Long userId, ChangePasswordRequest request) {
        Customer customer = customerRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Customer not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), customer.getPasswordHash())) {
            throw new RuntimeException("Current password is incorrect");
        }

        String newPasswordHash = passwordEncoder.encode(request.getNewPassword());
        customer.setPasswordHash(newPasswordHash);
        customerRepository.save(customer);
    }
}
