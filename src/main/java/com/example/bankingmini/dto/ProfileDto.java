package com.example.bankingmini.dto;

import lombok.*;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileDto {
    private Long id;
    private String email;
    private String name;
    private String phone;
    private String address;
    private String dateOfBirth;
    private String role;
    private Instant createdAt;
}

