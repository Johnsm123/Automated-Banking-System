package com.example.bankingmini.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class AccountDtos {
    public record CreateAccountRequest(
            @NotBlank @Size(min = 2, max = 50) @Pattern(regexp = "^[A-Z_]+$", message = "Account type must contain only uppercase letters and underscores") String accountType
    ) {
    }

    public record CreateAccountResponse(
            String accountNumber,
            String accountType,
            BigDecimal balance,
            String status
    ) {
    }

    public record MoneyRequest(
            @NotBlank @Pattern(regexp = "^[A-Z0-9]{10,20}$", message = "Invalid account number format") String accountNumber,
            @NotNull @DecimalMin(value = "0.01") @DecimalMax(value = "1000000.00") @Digits(integer = 16, fraction = 2) BigDecimal amount
    ) {
    }

    public record TransferRequest(
            @NotBlank @Pattern(regexp = "^[A-Z0-9]{10,20}$", message = "Invalid from account number format") String fromAccount,
            @NotBlank @Pattern(regexp = "^[A-Z0-9]{10,20}$", message = "Invalid to account number format") String toAccount,
            @NotNull @DecimalMin(value = "0.01") @DecimalMax(value = "1000000.00") @Digits(integer = 16, fraction = 2) BigDecimal amount
    ) {
    }

    public record BalanceResponse(String accountNumber, BigDecimal balance) {
    }

    public record TxnItem(String type, BigDecimal amount, String occurredAt) {
    }

    public record MiniStatementResponse(String accountNumber, List<TxnItem> last5) {
    }

    public record StatementRequest(@NotBlank @Pattern(regexp = "^[A-Z0-9]{10,20}$", message = "Invalid account number format") String accountNumber,
                                   @NotNull @PastOrPresent LocalDate fromDate,
                                   @NotNull @PastOrPresent LocalDate toDate,
                                   boolean csv // true => CSV response text, false => plain text
    ) {
    }

    public record StatementLine(String occurredAt, String type, BigDecimal amount, String
    refAccount) {
    }

    public record StatementResponse(
            String accountNumber,
            String fromDate,
            String toDate,
            BigDecimal openingBalance,
            BigDecimal closingBalance,
            List<StatementLine> lines,
            String contentType, // text/csv or text/plain
            String payload // server-generated printable text/CSV
    ) {
    }
    public record AccountSummaryDto(
            Long id,
            String accountNumber,
            BigDecimal balance,
            String accountType,
            String status,
            String createdAt
    ) {
    }
}
