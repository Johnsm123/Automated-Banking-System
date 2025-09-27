package com.example.bankingmini.service;

import com.example.bankingmini.dto.StudentLoanApplicationRequest;
import com.example.bankingmini.dto.StudentLoanDto;
import com.example.bankingmini.model.Account;
import com.example.bankingmini.model.Customer;
import com.example.bankingmini.model.StudentLoan;
import com.example.bankingmini.repository.AccountRepository;
import com.example.bankingmini.repository.CustomerRepository;
import com.example.bankingmini.repository.LoanInstallmentRepository;
import com.example.bankingmini.repository.StudentLoanRepository;
import com.example.bankingmini.repository.LoanRepository;
import com.example.bankingmini.repository.VehicleLoanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentLoanServiceTest {

    @Mock
    private StudentLoanRepository studentLoanRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private LoanInstallmentRepository installmentRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private VehicleLoanRepository vehicleLoanRepository;

    @InjectMocks
    private StudentLoanService studentLoanService;

    private Customer testCustomer;
    private Account testAccount;
    private StudentLoan testLoan;
    private StudentLoanApplicationRequest loanRequest;
    private final Long customerId = 1L;
    private final Long loanId = 1L;
    private final Long accountId = 1L;

    @BeforeEach
    void setUp() {
        testCustomer = Customer.builder()
                .id(customerId)
                .email("test@example.com")
                .name("Test User")
                .role("USER")
                .createdAt(Instant.now())
                .build();

        testAccount = Account.builder()
                .id(accountId)
                .accountNumber("ACC123456789")
                .balance(new BigDecimal("1000.00"))
                .customer(testCustomer)
                .status("ACTIVE")
                .createdAt(OffsetDateTime.now())
                .build();

        testLoan = StudentLoan.builder()
                .id(loanId)
                .customer(testCustomer)
                .account(testAccount)
                .loanAmount(new BigDecimal("50000.00"))
                .interestRate(new BigDecimal("8.5"))
                .tenureMonths(60)
                .courseName("Computer Science")
                .institutionName("Test University")
                .courseDurationYears(4)
                .courseFee(new BigDecimal("200000.00"))
                .academicYear("2024-2025")
                .studentName("John Doe")
                .studentAge(20)
                .guardianName("Jane Doe")
                .guardianIncome(new BigDecimal("500000.00"))
                .collateralProvided(true)
                .collateralDetails("Property worth 10 lakhs")
                .moratoriumPeriodMonths(6)
                .disbursementType("LUMP_SUM")
                .status("PENDING")
                .applicationDate(Instant.now())
                .outstandingAmount(new BigDecimal("50000.00"))
                .build();

        loanRequest = StudentLoanApplicationRequest.builder()
                .accountId(accountId)
                .loanAmount(new BigDecimal("50000.00"))
                .interestRate(new BigDecimal("8.5"))
                .tenureMonths(60)
                .courseName("Computer Science")
                .institutionName("Test University")
                .courseDurationYears(4)
                .courseFee(new BigDecimal("200000.00"))
                .academicYear("2024-2025")
                .studentName("John Doe")
                .studentAge(20)
                .guardianName("Jane Doe")
                .guardianIncome(new BigDecimal("500000.00"))
                .collateralProvided(true)
                .collateralDetails("Property worth 10 lakhs")
                .moratoriumPeriodMonths(6)
                .disbursementType("LUMP_SUM")
                .build();
    }

    // ---------------------- Tests ----------------------

    @Test
    void applyForLoan_Success() {
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(testCustomer));
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(testAccount));
        when(studentLoanRepository.countActiveLoansForAccount(accountId)).thenReturn(0L);
        when(loanRepository.countActiveLoansForAccount(accountId)).thenReturn(0L);
        when(vehicleLoanRepository.countActiveLoansForAccount(accountId)).thenReturn(0L);
        when(studentLoanRepository.save(any(StudentLoan.class))).thenReturn(testLoan);

        StudentLoanDto result = studentLoanService.applyForLoan(loanRequest, customerId);

        assertNotNull(result);
        assertEquals("Computer Science", result.getCourseName());
        assertEquals("PENDING", result.getStatus());
        verify(studentLoanRepository).save(any(StudentLoan.class));
    }

    @Test
    void applyForLoan_CustomerNotFound() {
        when(customerRepository.findById(customerId)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class,
                () -> studentLoanService.applyForLoan(loanRequest, customerId));
    }
    @Test
    void applyForLoan_AccountNotFound() {
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(testCustomer));
        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> studentLoanService.applyForLoan(loanRequest, customerId));
    }

    @Test
    void applyForLoan_AccountNotActive() {
        testAccount.setStatus("INACTIVE");
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(testCustomer));
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(testAccount));

        assertThrows(RuntimeException.class,
                () -> studentLoanService.applyForLoan(loanRequest, customerId));
    }



    @Test
    void getCustomerLoans_Success() {
        when(studentLoanRepository.findByCustomerIdOrderByApplicationDateDesc(customerId))
                .thenReturn(List.of(testLoan));

        List<StudentLoanDto> result = studentLoanService.getCustomerLoans(customerId);

        assertEquals(1, result.size());
        assertEquals(loanId, result.get(0).getId());
    }

    @Test
    void approveLoan_Success() {
        when(studentLoanRepository.findById(loanId)).thenReturn(Optional.of(testLoan));
        when(studentLoanRepository.save(any(StudentLoan.class))).thenReturn(testLoan);

        studentLoanService.approveLoan(loanId, 2L);

        assertEquals("APPROVED", testLoan.getStatus());
        assertNotNull(testLoan.getApprovalDate());
        assertNotNull(testLoan.getMonthlyEmi());
        verify(studentLoanRepository).save(testLoan);
    }

    @Test
    void approveLoan_NotPending() {
        testLoan.setStatus("APPROVED");
        when(studentLoanRepository.findById(loanId)).thenReturn(Optional.of(testLoan));

        assertThrows(RuntimeException.class,
                () -> studentLoanService.approveLoan(loanId, 2L));
    }

    @Test
    void rejectLoan_Success() {
        when(studentLoanRepository.findById(loanId)).thenReturn(Optional.of(testLoan));
        when(studentLoanRepository.save(any(StudentLoan.class))).thenReturn(testLoan);

        studentLoanService.rejectLoan(loanId, "Insufficient income", 2L);

        assertEquals("REJECTED", testLoan.getStatus());
        assertEquals("Insufficient income", testLoan.getRejectionReason());
        verify(studentLoanRepository).save(testLoan);
    }

    @Test
    void disburseLoan_Success() {
        testLoan.setStatus("APPROVED");
        testLoan.setNextDisbursementAmount(new BigDecimal("50000.00"));
        when(studentLoanRepository.findById(loanId)).thenReturn(Optional.of(testLoan));
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);
        when(studentLoanRepository.save(any(StudentLoan.class))).thenAnswer(invocation -> {
            StudentLoan loan = invocation.getArgument(0);
            loan.setDisbursementDate(Instant.now());
            return loan;
        });
        //invocation represents the method call that triggered the stub. It allows you to access the arguments passed to that method.
        //In disburseLoan(), the method modifies the loan object (sets disbursementDate and status).
        //Using thenAnswer allows us to capture the exact object passed to save(), modify it as the real repository would (or mimic side effects), and then return it.
        studentLoanService.disburseLoan(loanId);

        assertEquals("ACTIVE", testLoan.getStatus());
        assertEquals(new BigDecimal("51000.00"), testAccount.getBalance());
        verify(studentLoanRepository).save(testLoan);
    }

    @Test
    void disburseLoan_NotApproved() {
        when(studentLoanRepository.findById(loanId)).thenReturn(Optional.of(testLoan));
        assertThrows(RuntimeException.class,
                () -> studentLoanService.disburseLoan(loanId));
    }

    @Test
    void disburseLoan_NoAccountLinked() {
        testLoan.setStatus("APPROVED");
        testLoan.setAccount(null);
        when(studentLoanRepository.findById(loanId)).thenReturn(Optional.of(testLoan));

        assertThrows(RuntimeException.class,
                () -> studentLoanService.disburseLoan(loanId));
    }


    @Test
    void getPendingLoans_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        when(studentLoanRepository.findByStatusOrderByApplicationDateDesc("PENDING", pageable))
                .thenReturn(new PageImpl<>(List.of(testLoan)));

        Page<StudentLoanDto> result = studentLoanService.getPendingLoans(pageable);

        assertEquals(1, result.getContent().size());
        assertEquals("PENDING", result.getContent().get(0).getStatus());
    }

    @Test
    void renewLoan_Success() {
        testLoan.setStatus("ACTIVE");
        when(studentLoanRepository.findById(loanId)).thenReturn(Optional.of(testLoan));
        when(studentLoanRepository.save(any(StudentLoan.class))).thenReturn(testLoan);

        studentLoanService.renewLoan(loanId, new BigDecimal("10000.00"), 72);

        assertEquals(new BigDecimal("60000.00"), testLoan.getLoanAmount());
        assertEquals(Integer.valueOf(72), testLoan.getTenureMonths());
        verify(studentLoanRepository).save(testLoan);
    }

    @Test
    void renewLoan_NotActive() {
        when(studentLoanRepository.findById(loanId)).thenReturn(Optional.of(testLoan));
        assertThrows(RuntimeException.class,
                () -> studentLoanService.renewLoan(loanId, new BigDecimal("10000.00"), 72));
    }

    @Test
    void closeLoan_Success() {
        testLoan.setOutstandingAmount(BigDecimal.ZERO);
        when(studentLoanRepository.findById(loanId)).thenReturn(Optional.of(testLoan));
        when(studentLoanRepository.save(any(StudentLoan.class))).thenReturn(testLoan);

        studentLoanService.closeLoan(loanId, customerId);

        assertEquals("CLOSED", testLoan.getStatus());
        verify(studentLoanRepository).save(testLoan);
    }

    @Test
    void closeLoan_OutstandingAmount() {
        when(studentLoanRepository.findById(loanId)).thenReturn(Optional.of(testLoan));
        assertThrows(RuntimeException.class,
                () -> studentLoanService.closeLoan(loanId, customerId));
    }

    @Test
    void getAllLoans_WithStatus() {
        Pageable pageable = PageRequest.of(0, 10);
        when(studentLoanRepository.findByStatusOrderByApplicationDateDesc("PENDING", pageable))
                .thenReturn(new PageImpl<>(List.of(testLoan)));

        Page<StudentLoanDto> result = studentLoanService.getAllLoans("PENDING", pageable);

        assertEquals(1, result.getContent().size());
    }

    @Test
    void getAllLoans_WithoutStatus() {
        Pageable pageable = PageRequest.of(0, 10);
        when(studentLoanRepository.findAllByOrderByApplicationDateDesc(pageable))
                .thenReturn(new PageImpl<>(List.of(testLoan)));

        Page<StudentLoanDto> result = studentLoanService.getAllLoans(null, pageable);

        assertEquals(1, result.getContent().size());
    }

    @Test
    void getLoanInstallments_Success() {
        when(studentLoanRepository.findById(loanId)).thenReturn(Optional.of(testLoan));
        when(installmentRepository.findByLoanIdOrderByDueDateDesc(loanId))
                .thenReturn(List.of());

        assertDoesNotThrow(() -> studentLoanService.getLoanInstallments(loanId, customerId));
        verify(studentLoanRepository).findById(loanId);
    }

    @Test
    void payInstallment_AccessDenied() {
        when(studentLoanRepository.findById(loanId)).thenReturn(Optional.of(testLoan));

        assertThrows(RuntimeException.class,
                () -> studentLoanService.payInstallment(loanId, new BigDecimal("1000.00"), 999L));
    }
}
