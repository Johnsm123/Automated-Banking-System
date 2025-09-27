package com.example.bankingmini.service;

import com.example.bankingmini.dto.VehicleLoanApplicationRequest;
import com.example.bankingmini.dto.VehicleLoanDto;
import com.example.bankingmini.model.Account;
import com.example.bankingmini.model.Customer;
import com.example.bankingmini.model.VehicleLoan;
import com.example.bankingmini.repository.AccountRepository;
import com.example.bankingmini.repository.CustomerRepository;
import com.example.bankingmini.repository.LoanInstallmentRepository;
import com.example.bankingmini.repository.VehicleLoanRepository;
import com.example.bankingmini.repository.LoanRepository;
import com.example.bankingmini.repository.StudentLoanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehicleLoanServiceTest {

    @Mock
    private VehicleLoanRepository vehicleLoanRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private LoanInstallmentRepository installmentRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private StudentLoanRepository studentLoanRepository;

    @InjectMocks
    private VehicleLoanService vehicleLoanService;

    private Customer testCustomer;
    private Account testAccount;
    private VehicleLoan testLoan;
    private VehicleLoanApplicationRequest loanRequest;
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
                .balance(new BigDecimal("10000.00"))
                .customer(testCustomer)
                .status("ACTIVE")
                .createdAt(OffsetDateTime.now())
                .build();

        testLoan = VehicleLoan.builder()
                .id(loanId)
                .customer(testCustomer)
                .account(testAccount)
                .loanAmount(new BigDecimal("500000.00"))
                .interestRate(new BigDecimal("9.5"))
                .tenureMonths(60)
                .monthlyEmi(new BigDecimal("10500.00"))
                .vehicleType("CAR")
                .vehicleMake("Toyota")
                .vehicleModel("Camry")
                .vehicleYear(2024)
                .vehiclePrice(new BigDecimal("600000.00"))
                .downPayment(new BigDecimal("100000.00"))
                .monthlyIncome(new BigDecimal("50000.00"))
                .employmentType("SALARIED")
                .status("PENDING")
                .applicationDate(Instant.now())
                .outstandingAmount(new BigDecimal("500000.00"))
                .build();

        loanRequest = new VehicleLoanApplicationRequest();
        loanRequest.setAccountId(accountId);
        loanRequest.setLoanAmount(new BigDecimal("500000.00"));
        loanRequest.setInterestRate(new BigDecimal("9.5"));
        loanRequest.setTenureMonths(60);
        loanRequest.setVehicleType("CAR");
        loanRequest.setVehicleMake("Toyota");
        loanRequest.setVehicleModel("Camry");
        loanRequest.setVehicleYear(2024);
        loanRequest.setVehiclePrice(new BigDecimal("600000.00"));
        loanRequest.setDownPayment(new BigDecimal("100000.00"));
        loanRequest.setMonthlyIncome(new BigDecimal("50000.00"));
        loanRequest.setEmploymentType("SALARIED");
        loanRequest.setIncomeProof("Salary slips");
    }

    @Test
    void applyForLoan_Success() {
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(testCustomer));
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(testAccount));
        when(vehicleLoanRepository.countActiveLoansForAccount(accountId)).thenReturn(0L);
        when(loanRepository.countActiveLoansForAccount(accountId)).thenReturn(0L);
        when(studentLoanRepository.countActiveLoansForAccount(accountId)).thenReturn(0L);
        when(vehicleLoanRepository.save(any(VehicleLoan.class))).thenReturn(testLoan);

        VehicleLoanDto result = vehicleLoanService.applyForLoan(loanRequest, customerId);

        assertNotNull(result);
        assertEquals("Toyota", result.getVehicleMake());
        assertEquals("PENDING", result.getStatus());
        verify(vehicleLoanRepository).save(any(VehicleLoan.class));
    }

    @Test
    void getCustomerLoans_Success() {
        when(vehicleLoanRepository.findByCustomerIdOrderByApplicationDateDesc(customerId))
                .thenReturn(List.of(testLoan));

        List<VehicleLoanDto> result = vehicleLoanService.getCustomerLoans(customerId);

        assertEquals(1, result.size());
        assertEquals(loanId, result.get(0).getId());
    }

    @Test
    void approveLoan_Success() {
        when(vehicleLoanRepository.findById(loanId)).thenReturn(Optional.of(testLoan));
        when(vehicleLoanRepository.save(any(VehicleLoan.class))).thenReturn(testLoan);

        vehicleLoanService.approveLoan(loanId, 2L);

        assertEquals("APPROVED", testLoan.getStatus());
        assertNotNull(testLoan.getApprovalDate());
        verify(vehicleLoanRepository).save(testLoan);
    }

    @Test
    void rejectLoan_Success() {
        when(vehicleLoanRepository.findById(loanId)).thenReturn(Optional.of(testLoan));
        when(vehicleLoanRepository.save(any(VehicleLoan.class))).thenReturn(testLoan);

        vehicleLoanService.rejectLoan(loanId, "Insufficient income", 2L);

        assertEquals("REJECTED", testLoan.getStatus());
        assertEquals("Insufficient income", testLoan.getRejectionReason());
        verify(vehicleLoanRepository).save(testLoan);
    }

    @Test
    void disburseLoan_Success() {
        testLoan.setStatus("APPROVED");
        when(vehicleLoanRepository.findById(loanId)).thenReturn(Optional.of(testLoan));
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);
        when(vehicleLoanRepository.save(any(VehicleLoan.class))).thenReturn(testLoan);

        vehicleLoanService.disburseLoan(loanId);

        assertEquals("ACTIVE", testLoan.getStatus());
        assertNotNull(testLoan.getDisbursementDate());
        verify(vehicleLoanRepository).save(testLoan);
    }

    @Test
    void getPendingLoans_Success() {
        when(vehicleLoanRepository.findByStatusOrderByApplicationDateDesc("PENDING", PageRequest.of(0, 10)))
                .thenReturn(new PageImpl<>(List.of(testLoan)));

        var result = vehicleLoanService.getPendingLoans(PageRequest.of(0, 10));

        assertEquals(1, result.getContent().size());
        assertEquals("PENDING", result.getContent().get(0).getStatus());
    }

    @Test
    void renewLoan_Success() {
        testLoan.setStatus("ACTIVE");
        when(vehicleLoanRepository.findById(loanId)).thenReturn(Optional.of(testLoan));
        when(vehicleLoanRepository.save(any(VehicleLoan.class))).thenReturn(testLoan);

        vehicleLoanService.renewLoan(loanId, new BigDecimal("50000.00"), 72);

        assertEquals(new BigDecimal("550000.00"), testLoan.getLoanAmount());
        assertEquals(Integer.valueOf(72), testLoan.getTenureMonths());
        verify(vehicleLoanRepository).save(testLoan);
    }

    @Test
    void closeLoan_Success() {
        testLoan.setOutstandingAmount(BigDecimal.ZERO);
        when(vehicleLoanRepository.findById(loanId)).thenReturn(Optional.of(testLoan));
        when(vehicleLoanRepository.save(any(VehicleLoan.class))).thenReturn(testLoan);

        vehicleLoanService.closeLoan(loanId, customerId);

        assertEquals("CLOSED", testLoan.getStatus());
        verify(vehicleLoanRepository).save(testLoan);
    }

    @Test
    void getLoanInstallments_Success() {
        when(vehicleLoanRepository.findById(loanId)).thenReturn(Optional.of(testLoan));
        when(installmentRepository.findByLoanIdOrderByDueDateDesc(loanId))
                .thenReturn(List.of());

        assertDoesNotThrow(() -> vehicleLoanService.getLoanInstallments(loanId, customerId));
        verify(vehicleLoanRepository).findById(loanId);
    }

    @Test
    void getAllLoans_Success() {
        when(vehicleLoanRepository.findAllByOrderByApplicationDateDesc(PageRequest.of(0, 10)))
                .thenReturn(new PageImpl<>(List.of(testLoan)));

        var result = vehicleLoanService.getAllLoans(null, PageRequest.of(0, 10));

        assertEquals(1, result.getContent().size());
    }
}