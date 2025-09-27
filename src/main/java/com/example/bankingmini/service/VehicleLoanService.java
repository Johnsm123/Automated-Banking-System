package com.example.bankingmini.service;

import com.example.bankingmini.dto.LoanInstallmentDto;
import com.example.bankingmini.dto.VehicleLoanApplicationRequest;
import com.example.bankingmini.dto.VehicleLoanDto;
import com.example.bankingmini.model.Customer;
import com.example.bankingmini.repository.CustomerRepository;
import com.example.bankingmini.model.Account;
import com.example.bankingmini.repository.AccountRepository;
import com.example.bankingmini.model.LoanInstallment;
import com.example.bankingmini.model.VehicleLoan;
import com.example.bankingmini.repository.LoanInstallmentRepository;
import com.example.bankingmini.repository.LoanRepository;
import com.example.bankingmini.repository.StudentLoanRepository;
import com.example.bankingmini.repository.VehicleLoanRepository;
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
import java.util.stream.Collectors;

@Service
@Transactional
public class VehicleLoanService {

    @Autowired
    private VehicleLoanRepository vehicleLoanRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private LoanInstallmentRepository installmentRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private StudentLoanRepository studentLoanRepository;

    public VehicleLoanDto applyForLoan(VehicleLoanApplicationRequest request, Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

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
            long activeVehicleLoans = vehicleLoanRepository.countActiveLoansForAccount(account.getId());
            long activeGeneralLoans = loanRepository.countActiveLoansForAccount(account.getId());
            long activeStudentLoans = studentLoanRepository.countActiveLoansForAccount(account.getId());
            
            if (activeVehicleLoans > 0 || activeGeneralLoans > 0 || activeStudentLoans > 0) {
                throw new RuntimeException("Cannot apply for new loan. This account already has an active loan that is not closed.");
            }
        }

        // Calculate EMI
        BigDecimal monthlyEmi = calculateEMI(request.getLoanAmount(), request.getInterestRate(), request.getTenureMonths());

        VehicleLoan loan = VehicleLoan.builder()
                .customer(customer)
                .account(account) // Added account relationship
                .loanAmount(request.getLoanAmount())
                .interestRate(request.getInterestRate())
                .tenureMonths(request.getTenureMonths())
                .monthlyEmi(monthlyEmi)
                .vehicleType(request.getVehicleType())
                .vehicleMake(request.getVehicleMake())
                .vehicleModel(request.getVehicleModel())
                .vehicleYear(request.getVehicleYear())
                .vehiclePrice(request.getVehiclePrice())
                .downPayment(request.getDownPayment())
                .monthlyIncome(request.getMonthlyIncome())
                .employmentType(request.getEmploymentType())
                .incomeProof(request.getIncomeProof())
                .applicationDate(Instant.now())
                .outstandingAmount(request.getLoanAmount())
                .build();

        VehicleLoan savedLoan = vehicleLoanRepository.save(loan);
        return convertToDto(savedLoan);
    }

    public List<VehicleLoanDto> getCustomerLoans(Long customerId) {
        List<VehicleLoan> loans = vehicleLoanRepository.findByCustomerIdOrderByApplicationDateDesc(customerId);
        return loans.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    public VehicleLoanDto getLoanDetails(Long loanId, Long customerId) {
        VehicleLoan loan = vehicleLoanRepository.findById(loanId)
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


    public Page<VehicleLoanDto> getPendingLoans(Pageable pageable) {
        Page<VehicleLoan> loans = vehicleLoanRepository.findByStatusOrderByApplicationDateDesc("PENDING", pageable);
        return loans.map(this::convertToDto);
    }

    public void approveLoan(Long loanId, Long officerId) {
        VehicleLoan loan = vehicleLoanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        if (!loan.isPending()) {
            throw new RuntimeException("Loan is not in pending status");
        }

        loan.setStatus("APPROVED");
        loan.setApprovalDate(Instant.now());
        loan.setApprovedBy(officerId);
        vehicleLoanRepository.save(loan);
    }

    public void rejectLoan(Long loanId, String reason, Long officerId) {
        VehicleLoan loan = vehicleLoanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        if (!loan.isPending()) {
            throw new RuntimeException("Loan is not in pending status");
        }

        loan.setStatus("REJECTED");
        loan.setRejectionReason(reason);
        loan.setApprovedBy(officerId);
        vehicleLoanRepository.save(loan);
    }

    public void disburseLoan(Long loanId) {
        VehicleLoan loan = vehicleLoanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        if (!loan.isApproved()) {
            throw new RuntimeException("Loan is not approved");
        }

        if (loan.getAccount() != null) {
            Account account = loan.getAccount();
            account.setBalance(account.getBalance().add(loan.getLoanAmount()));
            accountRepository.save(account);
        }

        loan.setStatus("ACTIVE"); // Changed from DISBURSED to ACTIVE to match student loan lifecycle
        loan.setDisbursementDate(Instant.now());
        loan.setEmiStartDate(Instant.now().plus(30, ChronoUnit.DAYS));
        vehicleLoanRepository.save(loan);
    }

    public void payInstallment(Long loanId, BigDecimal amount, Long customerId) {
        VehicleLoan loan = vehicleLoanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        if (!loan.getCustomer().getId().equals(customerId)) {
            throw new RuntimeException("Access denied: Loan does not belong to user");
        }
        if (!loan.isActive()) {
            throw new RuntimeException("Installment cannot be paid: Loan not active");
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
                .loanType("VEHICLE")
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

        vehicleLoanRepository.save(loan);
    }

    public void renewLoan(Long loanId, BigDecimal additionalAmount, Integer newTenure) {
        VehicleLoan loan = vehicleLoanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        if (!loan.isActive()) {
            throw new RuntimeException("Only active loans can be renewed");
        }

        BigDecimal newLoanAmount = loan.getOutstandingAmount().add(additionalAmount);
        BigDecimal newEmi = calculateEMI(newLoanAmount, loan.getInterestRate(), newTenure);

        loan.setLoanAmount(newLoanAmount);
        loan.setOutstandingAmount(newLoanAmount);
        loan.setTenureMonths(newTenure);
        loan.setMonthlyEmi(newEmi);

        vehicleLoanRepository.save(loan);
    }

    public void closeLoan(Long loanId, Long customerId) {
        VehicleLoan loan = vehicleLoanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        if (!loan.getCustomer().getId().equals(customerId)) {
            throw new RuntimeException("Access denied: Loan does not belong to user");
        }

        if (loan.getOutstandingAmount().compareTo(BigDecimal.ZERO) > 0) {
            throw new RuntimeException("Cannot close loan with outstanding amount");
        }

        loan.setStatus("CLOSED");
        vehicleLoanRepository.save(loan);
    }

    public List<LoanInstallmentDto> getLoanInstallments(Long loanId, Long customerId) {
        VehicleLoan loan = vehicleLoanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        if (!loan.getCustomer().getId().equals(customerId)) {
            throw new RuntimeException("Access denied: Loan does not belong to user");
        }

        List<LoanInstallment> installments = installmentRepository.findByLoanIdOrderByDueDateDesc(loanId);
        return installments.stream().map(this::convertInstallmentToDto).collect(Collectors.toList());
    }

    private BigDecimal calculateEMI(BigDecimal principal, BigDecimal annualRate, Integer tenureMonths) {
        BigDecimal monthlyRate = annualRate.divide(BigDecimal.valueOf(1200), 10, RoundingMode.HALF_UP);
        BigDecimal onePlusR = BigDecimal.ONE.add(monthlyRate);
        BigDecimal numerator = principal.multiply(monthlyRate).multiply(onePlusR.pow(tenureMonths));
        BigDecimal denominator = onePlusR.pow(tenureMonths).subtract(BigDecimal.ONE);
        return numerator.divide(denominator, 2, RoundingMode.HALF_UP);
    }

    public Page<VehicleLoanDto> getAllLoans(String status, Pageable pageable) {
        Page<VehicleLoan> loans;
        if (status != null && !status.isEmpty()) {
            loans = vehicleLoanRepository.findByStatusOrderByApplicationDateDesc(status, pageable);
        } else {
            loans = vehicleLoanRepository.findAllByOrderByApplicationDateDesc(pageable);
        }
        return loans.map(this::convertToDto);
    }

    private VehicleLoanDto convertToDto(VehicleLoan loan) {
        return VehicleLoanDto.builder()
                .id(loan.getId())
                .customerId(loan.getCustomer().getId())
                .customerName(loan.getCustomer().getName())
                .customerEmail(loan.getCustomer().getEmail())
                .accountId(loan.getAccount() != null ? loan.getAccount().getId() : null) // Added account ID to DTO
                .accountNumber(loan.getAccount() != null ? loan.getAccount().getAccountNumber() : null) // Added account number to DTO
                .loanAmount(loan.getLoanAmount())
                .interestRate(loan.getInterestRate())
                .tenureMonths(loan.getTenureMonths())
                .monthlyEmi(loan.getMonthlyEmi())
                .vehicleType(loan.getVehicleType())
                .vehicleMake(loan.getVehicleMake())
                .vehicleModel(loan.getVehicleModel())
                .vehicleYear(loan.getVehicleYear())
                .vehiclePrice(loan.getVehiclePrice())
                .downPayment(loan.getDownPayment())
                .status(loan.getStatus())
                .applicationDate(loan.getApplicationDate())
                .approvalDate(loan.getApprovalDate())
                .disbursementDate(loan.getDisbursementDate())
                .emiStartDate(loan.getEmiStartDate()) // Added EMI start date to DTO
                .outstandingAmount(loan.getOutstandingAmount())
                .monthlyIncome(loan.getMonthlyIncome())
                .employmentType(loan.getEmploymentType())
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
