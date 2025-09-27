package com.example.bankingmini.dto;

import jakarta.validation.constraints.*;

public class JwtAuthDtos {

    public record JwtAuthResponse(
            String accessToken,
            String refreshToken,
            String tokenType,
            Long expiresIn,
            UserInfo user
    ) {
        public JwtAuthResponse(String accessToken, String refreshToken, Long expiresIn, UserInfo user) {
            this(accessToken, refreshToken, "Bearer", expiresIn, user);
        }
    }

    public record UserInfo(
            Long id,
            String email,
            String name,
            String role,
            String dateOfBirth
    ) {}

    public record RefreshTokenRequest(
            @NotBlank String refreshToken
    ) {}

    public record RefreshTokenResponse(
            String accessToken,
            String tokenType,
            Long expiresIn
    ) {
        public RefreshTokenResponse(String accessToken, Long expiresIn) {
            this( accessToken,"Bearer", expiresIn);
        }
    }
}
