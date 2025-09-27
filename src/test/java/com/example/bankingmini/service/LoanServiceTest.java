package com.example.bankingmini.service;

import com.example.bankingmini.dto.LoanDtos;
import com.example.bankingmini.model.Account;
import com.example.bankingmini.model.Customer;
import com.example.bankingmini.model.Loan;
import com.example.bankingmini.repository.AccountRepository;
import com.example.bankingmini.repository.CustomerRepository;
import com.example.bankingmini.repository.LoanInstallmentRepository;
import com.example.bankingmini.repository.LoanRepository;
import com.example.bankingmini.repository.StudentLoanRepository;
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
class LoanServiceTest {

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private LoanInstallmentRepository installmentRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private StudentLoanRepository studentLoanRepository;

    @Mock
    private VehicleLoanRepository vehicleLoanRepository;

    @InjectMocks
    private LoanService loanService;

    private Customer testCustomer;
    private Account testAccount;
    private Loan testLoan;
    private LoanDtos.LoanApplicationRequest loanRequest;
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
                .build();

        testAccount = Account.builder()
                .id(accountId)
                .accountNumber("ACC123456789")
                .balance(new BigDecimal("1000.00"))
                .customer(testCustomer)
                .status("ACTIVE")
                .createdAt(OffsetDateTime.now())
                .build();

        testLoan = Loan.builder()
                .id(loanId)
                .customer(testCustomer)
                .account(testAccount)
                .principal(new BigDecimal("10000.00"))
                .interestRate(new BigDecimal("10.0"))
                .tenureMonths(12)
                .monthlyEmi(new BigDecimal("879.16"))
                .outstandingAmount(new BigDecimal("10000.00"))
                .type("PERSONAL")
                .status("PENDING")
                .createdAt(Instant.now())
                .build();

        loanRequest = new LoanDtos.LoanApplicationRequest();
        loanRequest.setAccountId(accountId);
        loanRequest.setPrincipal(new BigDecimal("10000.00"));
        loanRequest.setInterestRate(new BigDecimal("10.0"));
        loanRequest.setTenureMonths(12);
        loanRequest.setType("PERSONAL");
    }

    @Test
    void applyForLoan_Success() {
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(testCustomer));
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(testAccount));
        when(loanRepository.countActiveLoansForAccount(accountId)).thenReturn(0L);
        when(studentLoanRepository.countActiveLoansForAccount(accountId)).thenReturn(0L);
        when(vehicleLoanRepository.countActiveLoansForAccount(accountId)).thenReturn(0L);
        when(loanRepository.save(any(Loan.class))).thenReturn(testLoan);

        LoanDtos.LoanDto result = loanService.applyForLoan(loanRequest, customerId);

        assertNotNull(result);
        assertEquals("PERSONAL", result.getType());
        assertEquals("PENDING", result.getStatus());
        verify(loanRepository).save(any(Loan.class));
    }

    @Test
    void applyForLoan_InvalidType() {
        loanRequest.setType("INVALID_TYPE");
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(testCustomer));

        assertThrows(IllegalArgumentException.class,
                () -> loanService.applyForLoan(loanRequest, customerId));
    }

    @Test
    void applyForLoan_NegativeInterestRate() {
        loanRequest.setInterestRate(new BigDecimal("-5.0"));
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(testCustomer));

        assertThrows(IllegalArgumentException.class,
                () -> loanService.applyForLoan(loanRequest, customerId));
    }

    @Test
    void getCustomerLoans_Success() {
        when(loanRepository.findByCustomerIdOrderByCreatedAtDesc(customerId))
                .thenReturn(List.of(testLoan));

        List<LoanDtos.LoanDto> result = loanService.getCustomerLoans(customerId);

        assertEquals(1, result.size());
        assertEquals(loanId, result.get(0).getId());
    }

    @Test
    void approveLoan_Success() {
        when(loanRepository.findById(loanId)).thenReturn(Optional.of(testLoan));
        when(loanRepository.save(any(Loan.class))).thenReturn(testLoan);

        loanService.approveLoan(loanId, 2L);

        assertEquals("APPROVED", testLoan.getStatus());
        assertNotNull(testLoan.getApprovedAt());
        verify(loanRepository).save(testLoan);
    }

    @Test
    void approveLoan_NotPending() {
        testLoan.setStatus("APPROVED");
        when(loanRepository.findById(loanId)).thenReturn(Optional.of(testLoan));

        assertThrows(RuntimeException.class,
                () -> loanService.approveLoan(loanId, 2L));
    }

    @Test
    void rejectLoan_Success() {
        when(loanRepository.findById(loanId)).thenReturn(Optional.of(testLoan));
        when(loanRepository.save(any(Loan.class))).thenReturn(testLoan);

        loanService.rejectLoan(loanId, "Insufficient income", 2L);

        assertEquals("REJECTED", testLoan.getStatus());
        assertEquals("Insufficient income", testLoan.getRejectionReason());
        verify(loanRepository).save(testLoan);
    }

    @Test
    void disburseLoan_Success() {
        testLoan.setStatus("APPROVED");
        when(loanRepository.findById(loanId)).thenReturn(Optional.of(testLoan));
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);
        when(loanRepository.save(any(Loan.class))).thenReturn(testLoan);

        loanService.disburseLoan(loanId);

        assertEquals("ACTIVE", testLoan.getStatus());
        assertNotNull(testLoan.getDisbursementDate());
        assertEquals(new BigDecimal("11000.00"), testAccount.getBalance());
        verify(loanRepository).save(testLoan);
    }

    @Test
    void disburseLoan_NotApproved() {
        when(loanRepository.findById(loanId)).thenReturn(Optional.of(testLoan));

        assertThrows(RuntimeException.class,
                () -> loanService.disburseLoan(loanId));
    }

    @Test
    void getPendingLoans_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        when(loanRepository.findByStatusOrderByCreatedAtDesc("PENDING", pageable))
                .thenReturn(new PageImpl<>(List.of(testLoan)));

        Page<LoanDtos.LoanDto> result = loanService.getPendingLoans(pageable);

        assertEquals(1, result.getContent().size());
        assertEquals("PENDING", result.getContent().get(0).getStatus());
    }

    @Test
    void request_Success() {
        when(loanRepository.save(any(Loan.class))).thenReturn(testLoan);

        Loan result = loanService.request(testCustomer, new BigDecimal("10000"), "PERSONAL", new BigDecimal("10.0"), 12);

        assertNotNull(result);
        assertEquals("PENDING", result.getStatus());
        verify(loanRepository).save(any(Loan.class));
    }

    @Test
    void approve_Success() {
        Customer admin = Customer.builder().role("ADMIN").build();
        when(loanRepository.findById(loanId)).thenReturn(Optional.of(testLoan));
        when(loanRepository.save(any(Loan.class))).thenReturn(testLoan);

        Loan result = loanService.approve(loanId, admin);

        assertEquals("APPROVED", result.getStatus());
        verify(loanRepository).save(testLoan);
    }

    @Test
    void approve_NotAdmin() {
        Customer user = Customer.builder().role("USER").build();

        assertThrows(IllegalArgumentException.class,
                () -> loanService.approve(loanId, user));
    }

    @Test
    void reject_Success() {
        Customer admin = Customer.builder().role("ADMIN").build();
        when(loanRepository.findById(loanId)).thenReturn(Optional.of(testLoan));
        when(loanRepository.save(any(Loan.class))).thenReturn(testLoan);

        Loan result = loanService.reject(loanId, admin);

        assertEquals("REJECTED", result.getStatus());
        verify(loanRepository).save(testLoan);
    }

    @Test
    void getLoanById_Success() {
        Customer admin = Customer.builder().role("ADMIN").build();
        when(loanRepository.findById(loanId)).thenReturn(Optional.of(testLoan));

        Loan result = loanService.getLoanById(loanId, admin);

        assertEquals(testLoan, result);
    }

    @Test
    void getLoanById_NotAdmin() {
        Customer user = Customer.builder().role("USER").build();

        assertThrows(IllegalArgumentException.class,
                () -> loanService.getLoanById(loanId, user));
    }

    @Test
    void testGetPendingLoans_Success() {
        Customer admin = Customer.builder().role("ADMIN").build();
        when(loanRepository.findByStatus("PENDING")).thenReturn(List.of(testLoan));

        List<Loan> result = loanService.getPendingLoans(admin);

        assertEquals(1, result.size());
        assertEquals(testLoan, result.get(0));
    }

    @Test
    void getAllLoans_WithStatus() {
        Pageable pageable = PageRequest.of(0, 10);
        when(loanRepository.findByStatusOrderByCreatedAtDesc("PENDING", pageable))
                .thenReturn(new PageImpl<>(List.of(testLoan)));

        Page<LoanDtos.LoanDto> result = loanService.getAllLoans("PENDING", pageable);

        assertEquals(1, result.getContent().size());
    }

    @Test
    void getAllLoans_WithoutStatus() {
        Pageable pageable = PageRequest.of(0, 10);
        when(loanRepository.findAllByOrderByCreatedAtDesc(pageable))
                .thenReturn(new PageImpl<>(List.of(testLoan)));

        Page<LoanDtos.LoanDto> result = loanService.getAllLoans(null, pageable);

        assertEquals(1, result.getContent().size());
    }
}