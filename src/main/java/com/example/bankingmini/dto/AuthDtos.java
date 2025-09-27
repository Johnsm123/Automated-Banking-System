package com.example.bankingmini.dto;

import jakarta.validation.constraints.*;

public class AuthDtos {

    //    public record RegisterRequest(
//            @Email @NotBlank @Size(max = 100) String email,
//            @NotBlank @Size(min = 8, max = 100) @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
//                    message = "Password must contain at least 8 characters with uppercase, lowercase, digit, and special character") String password,
//            @NotBlank @Size(min = 2, max = 100) @Pattern(regexp = "^[a-zA-Z\\s]+$", message = "Name must contain only letters and spaces") String name
//    ) {}
    public record RegisterRequest(
            @Email @NotBlank @Size(max = 100) String email,
            @NotBlank @Size(min = 8, max = 100)
            @Pattern(
                    regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#_])[A-Za-z\\d@$!%*?&#_]{8,}$",
                    message = "Password must contain at least 8 characters with uppercase, lowercase, digit, and special character"
            )
            String password,
            @NotBlank @Size(min = 2, max = 100)
            @Pattern(regexp = "^[a-zA-Z\\s]+$", message = "Name must contain only letters and spaces")
            String name,
            @NotBlank @Pattern(regexp = "^\\d{10}$", message = "Phone must be 10 digits")
            String phone,
            @NotBlank @Size(min = 10, max = 200)
            String address,
            @NotBlank @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "Date of birth must be in YYYY-MM-DD format")
            String dateOfBirth
    ) {
    }


    public record LoginRequest(
            @Email @NotBlank @Size(max = 100) String email,
            @NotBlank @Size(max = 100) String password
    ) {
    }

    public record MeResponse(Long id, String email, String name) {
    }
}
