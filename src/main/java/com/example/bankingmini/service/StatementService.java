package com.example.bankingmini.service;

import com.example.bankingmini.dto.AccountDtos;
import com.example.bankingmini.exception.NotFoundException;
import com.example.bankingmini.model.Account;
import com.example.bankingmini.model.TransactionEntity;
import com.example.bankingmini.repository.AccountRepository;
import com.example.bankingmini.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.*;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatementService {
    private final AccountRepository accounts;
    private final TransactionRepository txns;

    private BigDecimal apply(BigDecimal bal, TransactionEntity t) {
        return switch (t.getType()) {
            case "DEPOSIT", "TRANSFER_IN" -> bal.add(t.getAmount());
            case "WITHDRAW", "TRANSFER_OUT" -> bal.subtract(t.getAmount());
            default -> bal;
        };
    }

    private BigDecimal computeOpening(Account acc, LocalDate from) {
        var fromTs = from.atStartOfDay(ZoneId.systemDefault()).toInstant();
        var history = txns.findByAccountAndOccurredAtBeforeOrderByOccurredAtAsc(acc, fromTs);
        var bal = BigDecimal.ZERO;
        for (var t : history) {
            bal = apply(bal, t);
        }
        return bal;
    }

    public AccountDtos.StatementResponse generate(AccountDtos.StatementRequest req, Long userId) {
        var acc = accounts.findByAccountNumber(req.accountNumber())
                .orElseThrow(() -> new NotFoundException("Account not found: " + req.accountNumber()));

        // Verify the account belongs to the authenticated user
        if (!acc.getCustomer().getId().equals(userId)) {
            throw new IllegalArgumentException("Access denied: Account does not belong to user");
        }

        if (req.toDate().isBefore(req.fromDate())) {
            throw new IllegalArgumentException("toDate must be on/after fromDate");
        }

        var opening = computeOpening(acc, req.fromDate());
        var fromTs = req.fromDate().atStartOfDay(ZoneId.systemDefault()).toInstant();
        var toTs = req.toDate().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();

        var lines = txns.findByAccountAndOccurredAtBetweenOrderByOccurredAtAsc(acc, fromTs, toTs);

        var closing = opening;
        for (var t : lines) {
            closing = apply(closing, t);
        }

        var mapped = lines.stream().map(t ->
                new AccountDtos.StatementLine(
                        t.getOccurredAt().toString(),
                        t.getType(),
                        t.getAmount(),
                        t.getRefAccountId() == null ? "" : String.valueOf(t.getRefAccountId())
                )
        ).collect(Collectors.toList());

        String contentType = req.csv() ? "text/csv" : "text/plain";
        String payload = req.csv()
                ? toCsv(req.accountNumber(), req.fromDate(), req.toDate(), opening, closing, mapped)
                : toText(req.accountNumber(), req.fromDate(), req.toDate(), opening, closing, mapped);

        return new AccountDtos.StatementResponse(
                req.accountNumber(),
                req.fromDate().toString(),
                req.toDate().toString(),
                opening,
                closing,
                mapped,
                contentType,
                payload
        );
    }

    @Deprecated
    public AccountDtos.StatementResponse generate(AccountDtos.StatementRequest req) {
        throw new IllegalArgumentException("Unauthorized access: User ID required");
    }

    private String toCsv(String acc, LocalDate from, LocalDate to,
                         BigDecimal opening, BigDecimal closing, List<AccountDtos.StatementLine> lines) {
        var sb = new StringBuilder();
        sb.append("Account,From,To,Opening,Closing\n");
        sb.append(acc).append(",").append(from).append(",").append(to).append(",")
                .append(opening).append(",").append(closing).append("\n");
        sb.append("OccurredAt,Type,Amount,RefAccount\n");
        for (var l : lines) {
            sb.append(l.occurredAt()).append(",").append(l.type()).append(",")
                    .append(l.amount()).append(",")
                    .append(l.refAccount()).append("\n");
        }
        return sb.toString();
    }

    private String toText(String acc, LocalDate from, LocalDate to,
                          BigDecimal opening, BigDecimal closing, List<AccountDtos.StatementLine> lines) {
        var sb = new StringBuilder();
        sb.append("Statement for ").append(acc).append("\n")
                .append("Period: ").append(from).append(" to ").append(to).append("\n")
                .append("Opening: ").append(opening).append("\n");
        for (var l : lines) {
            sb.append(l.occurredAt()).append(" ")
                    .append(l.type()).append(" ")
                    .append(l.amount());
            if (!l.refAccount().isEmpty()) sb.append(" Ref:").append(l.refAccount());
            sb.append("\n");
        }
        sb.append("Closing: ").append(closing).append("\n");
        return sb.toString();
    }
}
