package com.example.bankingmini.service;

import com.example.bankingmini.dto.LoanDtos;
import com.example.bankingmini.dto.LoanInstallmentDto;
import com.example.bankingmini.model.Customer;
import com.example.bankingmini.repository.CustomerRepository;
import com.example.bankingmini.model.Account;
import com.example.bankingmini.repository.AccountRepository;
import com.example.bankingmini.exception.NotFoundException;
import com.example.bankingmini.model.Loan;
import com.example.bankingmini.model.LoanInstallment;
import com.example.bankingmini.repository.LoanInstallmentRepository;
import com.example.bankingmini.repository.LoanRepository;
import com.example.bankingmini.repository.StudentLoanRepository;
import com.example.bankingmini.repository.VehicleLoanRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class LoanService {

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private LoanInstallmentRepository installmentRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private StudentLoanRepository studentLoanRepository;

    @Autowired
    private VehicleLoanRepository vehicleLoanRepository;

    private static final Set<String> ALLOWED_TYPES =
            Set.of("EDUCATION", "STUDENT", "PERSONAL");

    private String normalizeType(String input) {
        var t = input.trim().toUpperCase();
        if (!ALLOWED_TYPES.contains(t)) {
            throw new IllegalArgumentException("Invalid loan type. Allowed: EDUCATION, STUDENT, PERSONAL");
        }
        return t;
    }

    public LoanDtos.LoanDto applyForLoan(LoanDtos.LoanApplicationRequest request, Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        var normalizedType = normalizeType(request.getType());

        if (request.getInterestRate().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Interest rate cannot be negative");
        }
        if (request.getTenureMonths() == null || request.getTenureMonths() < 1 || request.getTenureMonths() > 360) {
            throw new IllegalArgumentException("Tenure months must be 1..360");
        }

        Account account = null;
        if (request.getAccountId() != null) {
            account = accountRepository.findById(request.getAccountId())
                    .orElseThrow(() -> new RuntimeException("Account not found"));

            if (!account.getCustomer().getId().equals(customerId)) {
                throw new RuntimeException("Account does not belong to customer");
            }

            if (!"ACTIVE".equals(account.getStatus())) {
                throw new RuntimeException("Account must be active for loan application");
            }
        }

        // Check for existing active loans for this account across all loan types
        if (account != null) {
            long activeGeneralLoans = loanRepository.countActiveLoansForAccount(account.getId());
            long activeStudentLoans = studentLoanRepository.countActiveLoansForAccount(account.getId());
            long activeVehicleLoans = vehicleLoanRepository.countActiveLoansForAccount(account.getId());
            
            if (activeGeneralLoans > 0 || activeStudentLoans > 0 || activeVehicleLoans > 0) {
                throw new RuntimeException("Cannot apply for new loan. This account already has an active loan that is not closed.");
            }
        }

        BigDecimal monthlyEmi = calculateEMI(request.getPrincipal(), request.getInterestRate(), request.getTenureMonths());

        Loan loan = Loan.builder()
                .customer(customer)
                .account(account) // Added account relationship
                .principal(request.getPrincipal())
                .status("PENDING")
                .createdAt(Instant.now())
                .type(normalizedType)
                .interestRate(request.getInterestRate())
                .tenureMonths(request.getTenureMonths())
                .monthlyEmi(monthlyEmi)
                .outstandingAmount(request.getPrincipal())
                .build();

        Loan savedLoan = loanRepository.save(loan);
        return convertToDto(savedLoan);
    }

    public List<LoanDtos.LoanDto> getCustomerLoans(Long customerId) {
        List<Loan> loans = loanRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
        return loans.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    public LoanDtos.LoanDto getLoanDetails(Long loanId, Long customerId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        // Get current authentication
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

        boolean isLoanOfficer = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_LOAN_OFFICER"));

        // Only owner, admin, or loan officer can access
        if (!isAdmin && !isLoanOfficer && !loan.getCustomer().getId().equals(customerId)) {
            throw new RuntimeException("Access denied: Loan does not belong to user");
        }

        return convertToDto(loan);
    }


    public Page<LoanDtos.LoanDto> getPendingLoans(Pageable pageable) {
        Page<Loan> loans = loanRepository.findByStatusOrderByCreatedAtDesc("PENDING", pageable);
        return loans.map(this::convertToDto);
    }

    public void approveLoan(Long loanId, Long officerId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        if (!loan.isPending()) {
            throw new RuntimeException("Loan is not in pending status");
        }

        loan.setStatus("APPROVED");
        loan.setApprovedAt(Instant.now());
        loan.setApprovedBy(officerId);
        loanRepository.save(loan);
    }

    public void rejectLoan(Long loanId, String reason, Long officerId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        if (!loan.isPending()) {
            throw new RuntimeException("Loan is not in pending status");
        }

        loan.setStatus("REJECTED");
        loan.setRejectionReason(reason);
        loan.setApprovedBy(officerId);
        loanRepository.save(loan);
    }

    public void disburseLoan(Long loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        if (!loan.isApproved()) {
            throw new RuntimeException("Loan is not approved");
        }

        if (loan.getAccount() != null) {
            Account account = loan.getAccount();
            account.setBalance(account.getBalance().add(loan.getPrincipal()));
            accountRepository.save(account);
        }

        loan.setStatus("ACTIVE");
        loan.setDisbursementDate(Instant.now());
//        loan.setEmiStartDate(Instant.now().plus(30, ChronoUnit.DAYS));
        loan.setEmiStartDate(Instant.now().plus(1, ChronoUnit.MINUTES));
        loanRepository.save(loan);
    }

    public void payInstallment(Long loanId, BigDecimal amount, Long customerId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        if (!loan.getCustomer().getId().equals(customerId)) {
            throw new RuntimeException("Access denied: Loan does not belong to user");
        }
        if (!loan.isActive()) {
            throw new RuntimeException("Installment cannot be paid: Loan not active");
        }
        if (!loan.isDisbursed() && !loan.isActive()) {
            throw new RuntimeException("Installment cannot be paid: Loan not yet disbursed or not active");
        }
        if (loan.getEmiStartDate() != null && Instant.now().isBefore(loan.getEmiStartDate())) {
            throw new RuntimeException("Installment cannot be paid before EMI start date");
        }

        BigDecimal monthlyEmi = loan.getMonthlyEmi();
        BigDecimal outstanding = loan.getOutstandingAmount();

        if (outstanding.compareTo(monthlyEmi) > 0) {
            if (amount.compareTo(monthlyEmi) != 0) {
                throw new RuntimeException("Installment amount must equal monthly EMI: " + monthlyEmi);
            }
        } else {
            // Last installment: must pay exactly the remaining outstanding
            if (amount.compareTo(outstanding) != 0) {
                throw new RuntimeException("Final installment must equal outstanding amount: " + outstanding);
            }
        }

        int nextInstallmentNumber = installmentRepository.countByLoanId(loanId) + 1;

        // Create installment record
        LoanInstallment installment = LoanInstallment.builder()
                .loanId(loanId)
                .loanType("GENERAL")
                .installmentNumber(nextInstallmentNumber)
                .amount(amount)
                .paidAmount(amount)
                .paidDate(Instant.now())
                .dueDate(Instant.now())
                .status("PAID")
                .createdAt(Instant.now())
                .build();

        installmentRepository.save(installment);

        // Update outstanding amount
        BigDecimal newOutstanding = loan.getOutstandingAmount().subtract(amount);
        loan.setOutstandingAmount(newOutstanding);

        if (newOutstanding.compareTo(BigDecimal.ZERO) <= 0) {
            loan.setStatus("CLOSED");
        }

        loanRepository.save(loan);
    }

    public void renewLoan(Long loanId, BigDecimal additionalAmount, Integer newTenure) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        if (!loan.isActive()) {
            throw new RuntimeException("Only active loans can be renewed");
        }

        BigDecimal newLoanAmount = loan.getOutstandingAmount().add(additionalAmount);
        BigDecimal newEmi = calculateEMI(newLoanAmount, loan.getInterestRate(), newTenure);

        loan.setPrincipal(newLoanAmount);
        loan.setOutstandingAmount(newLoanAmount);
        loan.setTenureMonths(newTenure);
        loan.setMonthlyEmi(newEmi);

        loanRepository.save(loan);
    }

    public void closeLoan(Long loanId, Long customerId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        if (!loan.getCustomer().getId().equals(customerId)) {
            throw new RuntimeException("Access denied: Loan does not belong to user");
        }

        if (loan.getOutstandingAmount().compareTo(BigDecimal.ZERO) > 0) {
            throw new RuntimeException("Cannot close loan with outstanding amount");
        }

        loan.setStatus("CLOSED");
        loanRepository.save(loan);
    }

    public List<LoanInstallmentDto> getLoanInstallments(Long loanId, Long customerId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        if (!loan.getCustomer().getId().equals(customerId)) {
            throw new RuntimeException("Access denied: Loan does not belong to user");
        }

        List<LoanInstallment> installments = installmentRepository.findByLoanIdOrderByDueDateDesc(loanId);
        return installments.stream().map(this::convertInstallmentToDto).collect(Collectors.toList());
    }

    public Page<LoanDtos.LoanDto> getAllLoans(String status, Pageable pageable) {
        Page<Loan> loans;
        if (status != null && !status.isEmpty()) {
            loans = loanRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
        } else {
            loans = loanRepository.findAllByOrderByCreatedAtDesc(pageable);
        }
        return loans.map(this::convertToDto);
    }

    @Transactional
    public Loan request(Customer customer, BigDecimal principal, String type, BigDecimal
            interestRate, Integer tenureMonths) {
        var normalizedType = normalizeType(type);
        if (interestRate.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Interest rate cannot be negative");
        }
        if (tenureMonths == null || tenureMonths < 1 || tenureMonths > 360) {
            throw new IllegalArgumentException("Tenure months must be 1..360");
        }

        BigDecimal monthlyEmi = calculateEMI(principal, interestRate, tenureMonths);

        var loan = Loan.builder()
                .customer(customer)
                .principal(principal)
                .status("PENDING")
                .createdAt(Instant.now())
                .type(normalizedType)
                .interestRate(interestRate)
                .tenureMonths(tenureMonths)
                .monthlyEmi(monthlyEmi)
                .outstandingAmount(principal)
                .build();
        return loanRepository.save(loan);
    }

    @Transactional
    public Loan approve(Long id, Customer admin) {
        if (!admin.isAdmin()) {
            throw new IllegalArgumentException("Only admins can approve loans");
        }
        var loan = loanRepository.findById(id).orElseThrow(() -> new NotFoundException("Loan not found"));
        if (!"PENDING".equals(loan.getStatus())) {
            throw new IllegalArgumentException("Only pending loans can be approved");
        }
        loan.setStatus("APPROVED");
        loan.setApprovedAt(Instant.now());
        return loanRepository.save(loan);
    }

    @Transactional
    public Loan reject(Long id, Customer admin) {
        if (!admin.isAdmin()) {
            throw new IllegalArgumentException("Only admins can reject loans");
        }
        var loan = loanRepository.findById(id).orElseThrow(() -> new NotFoundException("Loan not found"));
        if (!"PENDING".equals(loan.getStatus())) {
            throw new IllegalArgumentException("Only pending loans can be rejected");
        }
        loan.setStatus("REJECTED");
        return loanRepository.save(loan);
    }


    public Loan getLoanById(Long id, Customer admin) {
        if (!admin.isAdmin()) {
            throw new IllegalArgumentException("Only admins can view loan details");
        }
        return loanRepository.findById(id).orElseThrow(() -> new NotFoundException("Loan not found"));
    }

    public List<Loan> getPendingLoans(Customer admin) {
        if (!admin.isAdmin()) {
            throw new IllegalArgumentException("Only admins can view pending loans");
        }
        return loanRepository.findByStatus("PENDING");
    }

    private BigDecimal calculateEMI(BigDecimal principal, BigDecimal annualRate, Integer tenureMonths) {
        BigDecimal monthlyRate = annualRate.divide(BigDecimal.valueOf(1200), 10, RoundingMode.HALF_UP);
        BigDecimal onePlusR = BigDecimal.ONE.add(monthlyRate);
        BigDecimal numerator = principal.multiply(monthlyRate).multiply(onePlusR.pow(tenureMonths));
        BigDecimal denominator = onePlusR.pow(tenureMonths).subtract(BigDecimal.ONE);
        return numerator.divide(denominator, 2, RoundingMode.HALF_UP);
    }

    private LoanDtos.LoanDto convertToDto(Loan loan) {
        return LoanDtos.LoanDto.builder()
                .id(loan.getId())
                .customerId(loan.getCustomer().getId())
                .customerName(loan.getCustomer().getName())
                .customerEmail(loan.getCustomer().getEmail())
                .accountId(loan.getAccount() != null ? loan.getAccount().getId() : null) // Added account ID to DTO
                .accountNumber(loan.getAccount() != null ? loan.getAccount().getAccountNumber() : null) // Added account number to DTO
                .principal(loan.getPrincipal())
                .interestRate(loan.getInterestRate())
                .tenureMonths(loan.getTenureMonths())
                .monthlyEmi(loan.getMonthlyEmi())
                .type(loan.getType())
                .status(loan.getStatus())
                .createdAt(loan.getCreatedAt())
                .approvedAt(loan.getApprovedAt())
                .disbursementDate(loan.getDisbursementDate())
                .emiStartDate(loan.getEmiStartDate())
                .outstandingAmount(loan.getOutstandingAmount())
                .rejectionReason(loan.getRejectionReason())
                .build();
    }

    private LoanInstallmentDto convertInstallmentToDto(LoanInstallment installment) {
        return LoanInstallmentDto.builder()
                .id(installment.getId())
                .loanId(installment.getLoanId())
                .loanType(installment.getLoanType())
                .installmentNumber(installment.getInstallmentNumber())
                .amount(installment.getAmount())
                .paidAmount(installment.getPaidAmount())
                .dueDate(installment.getDueDate())
                .paidDate(installment.getPaidDate())
                .status(installment.getStatus())
                .build();
    }
}
