package com.example.bankingmini.service;

import com.example.bankingmini.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionLimitService {

    private final TransactionRepository transactionRepository;

    // Business day time zone (configure if needed)
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Kolkata");

    // Transaction limits (in INR)
    private static final BigDecimal DEPOSIT_PER_TRANSACTION_LIMIT = new BigDecimal("100000");
    private static final BigDecimal DEPOSIT_DAILY_LIMIT = new BigDecimal("200000");

    private static final BigDecimal WITHDRAW_PER_TRANSACTION_LIMIT = new BigDecimal("100000");
    private static final BigDecimal WITHDRAW_DAILY_LIMIT = new BigDecimal("200000");

    private static final BigDecimal TRANSFER_PER_TRANSACTION_LIMIT = new BigDecimal("100000");
    private static final BigDecimal TRANSFER_DAILY_LIMIT = new BigDecimal("200000");

    public void validateDepositLimits(Long accountId, BigDecimal amount) {
        requirePositiveAmount(amount);

        if (amount.compareTo(DEPOSIT_PER_TRANSACTION_LIMIT) > 0) {
            throw new IllegalArgumentException(
                    "Deposit amount exceeds per-transaction limit of ₹" + DEPOSIT_PER_TRANSACTION_LIMIT.toPlainString()
            );
        }

        BigDecimal dailyDeposits = getDailyTransactionSum(accountId, "DEPOSIT");
        if (dailyDeposits.add(amount).compareTo(DEPOSIT_DAILY_LIMIT) > 0) {
            throw new IllegalArgumentException(
                    "Deposit amount exceeds daily limit of ₹" + DEPOSIT_DAILY_LIMIT.toPlainString()
                            + ". Today's deposits: ₹" + dailyDeposits.toPlainString()
            );
        }
    }

    public void validateWithdrawLimits(Long accountId, BigDecimal amount) {
        requirePositiveAmount(amount);

        if (amount.compareTo(WITHDRAW_PER_TRANSACTION_LIMIT) > 0) {
            throw new IllegalArgumentException(
                    "Withdrawal amount exceeds per-transaction limit of ₹" + WITHDRAW_PER_TRANSACTION_LIMIT.toPlainString()
            );
        }

        BigDecimal dailyWithdrawals = getDailyTransactionSum(accountId, "WITHDRAW");
        if (dailyWithdrawals.add(amount).compareTo(WITHDRAW_DAILY_LIMIT) > 0) {
            throw new IllegalArgumentException(
                    "Withdrawal amount exceeds daily limit of ₹" + WITHDRAW_DAILY_LIMIT.toPlainString()
                            + ". Today's withdrawals: ₹" + dailyWithdrawals.toPlainString()
            );
        }
    }

    public void validateTransferLimits(Long accountId, BigDecimal amount) {
        requirePositiveAmount(amount);

        if (amount.compareTo(TRANSFER_PER_TRANSACTION_LIMIT) > 0) {
            throw new IllegalArgumentException(
                    "Transfer amount exceeds per-transaction limit of ₹" + TRANSFER_PER_TRANSACTION_LIMIT.toPlainString()
            );
        }

        BigDecimal dailyTransfers = getDailyTransactionSum(accountId, "TRANSFER_OUT");
        if (dailyTransfers.add(amount).compareTo(TRANSFER_DAILY_LIMIT) > 0) {
            throw new IllegalArgumentException(
                    "Transfer amount exceeds daily limit of ₹" + TRANSFER_DAILY_LIMIT.toPlainString()
                            + ". Today's transfers: ₹" + dailyTransfers.toPlainString()
            );
        }
    }

    private static void requirePositiveAmount(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("Amount must be a positive value.");
        }
    }

    private BigDecimal getDailyTransactionSum(Long accountId, String transactionType) {
        // Define the day in BUSINESS_ZONE
        LocalDate today = LocalDate.now(BUSINESS_ZONE);

        // Convert the local-day window to Instants
        Instant startInclusive = today.atStartOfDay(BUSINESS_ZONE).toInstant();
        Instant endExclusive   = today.plusDays(1).atStartOfDay(BUSINESS_ZONE).toInstant();

        BigDecimal sum = transactionRepository.sumDailyTransactionsByType(
                accountId,
                transactionType,
                startInclusive,
                endExclusive
        );

        return (sum != null) ? sum : BigDecimal.ZERO;
    }
}
