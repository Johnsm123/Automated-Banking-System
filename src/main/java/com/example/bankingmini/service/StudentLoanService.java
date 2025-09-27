package com.example.bankingmini.service;

import com.example.bankingmini.dto.LoanInstallmentDto;
import com.example.bankingmini.dto.StudentLoanApplicationRequest;
import com.example.bankingmini.dto.StudentLoanDto;
import com.example.bankingmini.exception.NotFoundException;
import com.example.bankingmini.model.Customer;
import com.example.bankingmini.repository.CustomerRepository;
import com.example.bankingmini.model.Account;
import com.example.bankingmini.repository.AccountRepository;
import com.example.bankingmini.model.LoanInstallment;
import com.example.bankingmini.model.StudentLoan;
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
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class StudentLoanService {

    @Autowired
    private StudentLoanRepository studentLoanRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private LoanInstallmentRepository installmentRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private VehicleLoanRepository vehicleLoanRepository;

    private final static String approved = "APPROVED";

    public StudentLoanDto applyForLoan(StudentLoanApplicationRequest request, Long customerId) {
        log.info("Processing student loan application for customer ID: {} with amount: {}", customerId, request.getLoanAmount());
        try {
            Customer customer = customerRepository.findById(customerId)
                    .orElseThrow(() -> {
                        log.error("Customer not found with ID: {}", customerId);
                        return new RuntimeException("Customer not found");
                    });

            Account account = null;
            if (request.getAccountId() != null) {
                account = accountRepository.findById(request.getAccountId())
                        .orElseThrow(() -> {
                            log.error("Account not found with ID: {}", request.getAccountId());
                            return new RuntimeException("Account not found");
                        });

                if (!account.getCustomer().getId().equals(customerId)) {
                    log.warn("Account {} does not belong to customer {}", request.getAccountId(), customerId);
                    throw new RuntimeException("Account does not belong to customer");
                }

                if (!"ACTIVE".equals(account.getStatus())) {
                    log.warn("Account {} is not active for loan application", request.getAccountId());
                    throw new RuntimeException("Account must be active for loan application");
                }
            }

            // Check for existing active loans for this account across all loan types
            if (account != null) {
                long activeStudentLoans = studentLoanRepository.countActiveLoansForAccount(account.getId());
                long activeGeneralLoans = loanRepository.countActiveLoansForAccount(account.getId());
                long activeVehicleLoans = vehicleLoanRepository.countActiveLoansForAccount(account.getId());
                
                if (activeStudentLoans > 0 || activeGeneralLoans > 0 || activeVehicleLoans > 0) {
                    log.warn("Account {} already has active loans - Student: {}, General: {}, Vehicle: {}", 
                            account.getId(), activeStudentLoans, activeGeneralLoans, activeVehicleLoans);
                    throw new RuntimeException("Cannot apply for new loan. This account already has an active loan that is not closed.");
                }
            }

            StudentLoan loan = StudentLoan.builder()
                    .customer(customer)
                    .account(account)
                    .loanAmount(request.getLoanAmount())
                    .interestRate(request.getInterestRate())
                    .tenureMonths(request.getTenureMonths())
                    .courseName(request.getCourseName())
                    .institutionName(request.getInstitutionName())
                    .courseDurationYears(request.getCourseDurationYears())
                    .courseFee(request.getCourseFee())
                    .academicYear(request.getAcademicYear())
                    .studentName(request.getStudentName())
                    .studentAge(request.getStudentAge())
                    .guardianName(request.getGuardianName())
                    .guardianIncome(request.getGuardianIncome())
                    .collateralProvided(request.getCollateralProvided())
                    .collateralDetails(request.getCollateralDetails())
                    .moratoriumPeriodMonths(request.getMoratoriumPeriodMonths())
                    .disbursementType(request.getDisbursementType())
                    .applicationDate(Instant.now())
                    .outstandingAmount(request.getLoanAmount())
                    .build();

            StudentLoan savedLoan = studentLoanRepository.save(loan);
            log.info("Successfully created student loan application with ID: {} for customer: {}", savedLoan.getId(), customerId);
            return convertToDto(savedLoan);
        } catch (Exception e) {
            log.error("Error processing student loan application for customer {}: {}", customerId, e.getMessage(), e);
            throw e;
        }
    }

    public List<StudentLoanDto> getCustomerLoans(Long customerId) {
        List<StudentLoan> loans = studentLoanRepository.findByCustomerIdOrderByApplicationDateDesc(customerId);
        return loans.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    public StudentLoanDto getLoanDetails(Long loanId, Long customerId) {
        StudentLoan loan = studentLoanRepository.findById(loanId)
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


    public Page<StudentLoanDto> getPendingLoans(Pageable pageable) {
        Page<StudentLoan> loans = studentLoanRepository.findByStatusOrderByApplicationDateDesc("PENDING", pageable);
        return loans.map(this::convertToDto);
    }

    public void approveLoan(Long loanId, Long officerId) {
        log.info("Approving student loan ID: {} by officer ID: {}", loanId, officerId);
        try {
            StudentLoan loan = studentLoanRepository.findById(loanId)
                    .orElseThrow(() -> {
                        log.error("Student loan not found with ID: {}", loanId);
                        return new RuntimeException("Loan not found");
                    });

            if (!loan.isPending()) {
                log.warn("Attempted to approve non-pending loan ID: {} with status: {}", loanId, loan.getStatus());
                throw new RuntimeException("Loan is not in pending status");
            }

            // Validate loan data
            if (loan.getLoanAmount() == null || loan.getInterestRate() == null || loan.getTenureMonths() == null) {
                log.error("Invalid loan data for ID: {} - amount: {}, rate: {}, tenure: {}", 
                        loanId, loan.getLoanAmount(), loan.getInterestRate(), loan.getTenureMonths());
                throw new RuntimeException("Invalid loan data - missing required fields");
            }

            // Calculate EMI (will start after moratorium period)
            BigDecimal monthlyEmi = calculateEMI(loan.getLoanAmount(), loan.getInterestRate(), loan.getTenureMonths());
            loan.setMonthlyEmi(monthlyEmi);

            // Approve loan
            loan.setStatus(approved);
            loan.setApprovalDate(Instant.now());
            loan.setApprovedBy(officerId);

            // Set next disbursement amount and date
            BigDecimal nextDisbursement = calculateNextDisbursement(loan);
            Instant nextDisbursementDate = calculateNextDisbursementDate(loan);
            
            loan.setNextDisbursementAmount(nextDisbursement);
            loan.setNextDisbursementDate(nextDisbursementDate);

            studentLoanRepository.save(loan);
            log.info("Successfully approved student loan ID: {} with EMI: {} and next disbursement: {}", 
                    loanId, monthlyEmi, nextDisbursement);
        } catch (Exception e) {
            log.error("Error approving student loan ID: {} by officer ID: {}: {}", loanId, officerId, e.getMessage(), e);
            throw e;
        }
    }

    public void rejectLoan(Long loanId, String reason, Long officerId) {
        StudentLoan loan = studentLoanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        if (!loan.isPending()) {
            throw new RuntimeException("Loan is not in pending status");
        }

        loan.setStatus("REJECTED");
        loan.setRejectionReason(reason);
        loan.setApprovedBy(officerId);
        studentLoanRepository.save(loan);
    }

    public void disburseLoan(Long loanId) {
        StudentLoan loan = studentLoanRepository.findById(loanId)
                .orElseThrow(() -> new NotFoundException("Loan not found"));

        if (!approved.equals(loan.getStatus())) {
            throw new NotFoundException("Loan is not approved yet");
        }

        // Fetch linked account
        Account account = loan.getAccount();
        if (account == null) {
            throw new NotFoundException("No account linked to this loan");
        }

        BigDecimal nextAmount = loan.getNextDisbursementAmount();
        if (nextAmount == null || nextAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new NotFoundException("Next disbursement amount is invalid");
        }

        // Add next disbursement to account balance
        account.setBalance(account.getBalance().add(nextAmount));
        accountRepository.save(account);

        // Deduct disbursed amount from outstanding
        loan.setOutstandingAmount(loan.getOutstandingAmount().subtract(nextAmount));

        // Update next disbursement for remaining amount
        if (loan.getOutstandingAmount().compareTo(BigDecimal.ZERO) > 0) {
            // Partially disbursed, keep status as APPROVED
            loan.setNextDisbursementAmount(calculateNextDisbursement(loan));
            loan.setNextDisbursementDate(calculateNextDisbursementDate(loan));
            loan.setStatus(approved);
        } else {
            // Fully disbursed
            loan.setNextDisbursementAmount(BigDecimal.ZERO);
            loan.setNextDisbursementDate(null);
            loan.setStatus("ACTIVE");
        }

        studentLoanRepository.save(loan);
    }


    private Instant calculateNextDisbursementDate(StudentLoan loan) {
        switch (loan.getDisbursementType()) {
            case "SEMESTER_WISE":
                return Instant.now().plus(180, ChronoUnit.DAYS); // ~6 months for next semester
            case "YEARLY":
                return Instant.now().plus(365, ChronoUnit.DAYS); // ~12 months for next year
            case "LUMP_SUM":
            default:
                return Instant.now().plus(7, ChronoUnit.DAYS); // Immediate for lump sum
        }
    }

    private BigDecimal calculateNextDisbursement(StudentLoan loan) {
        if (loan.getOutstandingAmount() == null || loan.getOutstandingAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        
        if ("SEMESTER_WISE".equals(loan.getDisbursementType())) {
            Integer courseDuration = loan.getCourseDurationYears();
            if (courseDuration == null || courseDuration <= 0) {
                courseDuration = 4; // Default to 4 years
            }
            return loan.getLoanAmount().divide(
                    BigDecimal.valueOf(courseDuration * 2L), 2, RoundingMode.HALF_UP);
        } else if ("YEARLY".equals(loan.getDisbursementType())) {
            Integer courseDuration = loan.getCourseDurationYears();
            if (courseDuration == null || courseDuration <= 0) {
                courseDuration = 4; // Default to 4 years
            }
            return loan.getLoanAmount().divide(
                    BigDecimal.valueOf(courseDuration), 2, RoundingMode.HALF_UP);
        } else {
            return loan.getLoanAmount(); // LUMP_SUM
        }
    }



    public void renewLoan(Long loanId, BigDecimal additionalAmount, Integer newTenure) {
        StudentLoan loan = studentLoanRepository.findById(loanId)
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

        studentLoanRepository.save(loan);
    }

    public void closeLoan(Long loanId, Long customerId) {
        StudentLoan loan = studentLoanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        if (!loan.getCustomer().getId().equals(customerId)) {
            throw new RuntimeException("Access denied: Loan does not belong to user");
        }

        if (loan.getOutstandingAmount().compareTo(BigDecimal.ZERO) > 0) {
            throw new RuntimeException("Cannot close loan with outstanding amount");
        }

        loan.setStatus("CLOSED");
        studentLoanRepository.save(loan);
    }

    public Page<StudentLoanDto> getAllLoans(String status, Pageable pageable) {
        Page<StudentLoan> loans;
        if (status != null && !status.isEmpty()) {
            loans = studentLoanRepository.findByStatusOrderByApplicationDateDesc(status, pageable);
        } else {
            loans = studentLoanRepository.findAllByOrderByApplicationDateDesc(pageable);
        }
        return loans.map(this::convertToDto);
    }

    public List<LoanInstallmentDto> getLoanInstallments(Long loanId, Long customerId) {
        StudentLoan loan = studentLoanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        if (!loan.getCustomer().getId().equals(customerId)) {
            throw new RuntimeException("Access denied: Loan does not belong to user");
        }

        List<LoanInstallment> installments = installmentRepository.findByLoanIdOrderByDueDateDesc(loanId);
        return installments.stream().map(this::convertInstallmentToDto).collect(Collectors.toList());
    }

    public void payInstallment(Long loanId, BigDecimal amount, Long customerId) {
        StudentLoan loan = studentLoanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        if (!loan.getCustomer().getId().equals(customerId)) {
            throw new RuntimeException("Access denied: Loan does not belong to user");
        }
        if (!loan.isDisbursed() && !loan.isActive()) {
            throw new RuntimeException("Installment cannot be paid: Loan not yet disbursed or not active");
        }
        if (loan.getEmiStartDate() == null || Instant.now().isBefore(loan.getEmiStartDate())) {
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
                .amount(amount)
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

        studentLoanRepository.save(loan);
    }

    private BigDecimal calculateEMI(BigDecimal principal, BigDecimal annualRate, Integer tenureMonths) {
        BigDecimal monthlyRate = annualRate.divide(BigDecimal.valueOf(1200), 10, RoundingMode.HALF_UP);
        BigDecimal onePlusR = BigDecimal.ONE.add(monthlyRate);
        BigDecimal numerator = principal.multiply(monthlyRate).multiply(onePlusR.pow(tenureMonths));
        BigDecimal denominator = onePlusR.pow(tenureMonths).subtract(BigDecimal.ONE);
        return numerator.divide(denominator, 2, RoundingMode.HALF_UP);
    }

    private StudentLoanDto convertToDto(StudentLoan loan) {
        return StudentLoanDto.builder()
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
                .courseName(loan.getCourseName())
                .institutionName(loan.getInstitutionName())
                .courseDurationYears(loan.getCourseDurationYears())
                .courseFee(loan.getCourseFee())
                .academicYear(loan.getAcademicYear())
                .studentName(loan.getStudentName())
                .studentAge(loan.getStudentAge())
                .guardianName(loan.getGuardianName())
                .guardianIncome(loan.getGuardianIncome())
                .collateralProvided(loan.getCollateralProvided())
                .collateralDetails(loan.getCollateralDetails())
                .status(loan.getStatus())
                .moratoriumPeriodMonths(loan.getMoratoriumPeriodMonths())
                .applicationDate(loan.getApplicationDate())
                .approvalDate(loan.getApprovalDate())
                .disbursementDate(loan.getDisbursementDate())
                .courseCompletionDate(loan.getCourseCompletionDate())
                .emiStartDate(loan.getEmiStartDate())
                .outstandingAmount(loan.getOutstandingAmount())
                .disbursementType(loan.getDisbursementType())
                .nextDisbursementAmount(loan.getNextDisbursementAmount())
                .nextDisbursementDate(loan.getNextDisbursementDate())
                .rejectionReason(loan.getRejectionReason())
                .build();
    }

    private LoanInstallmentDto convertInstallmentToDto(LoanInstallment installment) {
        return LoanInstallmentDto.builder()
                .id(installment.getId())
                .loanId(installment.getLoanId())
                .amount(installment.getAmount())
                .dueDate(installment.getDueDate())
                .paidDate(installment.getPaidDate())
                .status(installment.getStatus())
                .build();
    }
}
