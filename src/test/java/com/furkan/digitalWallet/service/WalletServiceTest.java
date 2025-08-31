package com.furkan.digitalWallet.service;

import com.furkan.digitalWallet.entity.Customer;
import com.furkan.digitalWallet.entity.Transaction;
import com.furkan.digitalWallet.entity.Wallet;
import com.furkan.digitalWallet.enums.*;
import com.furkan.digitalWallet.exception.BadRequestException;
import com.furkan.digitalWallet.exception.NotFoundException;
import com.furkan.digitalWallet.repository.CustomerRepository;
import com.furkan.digitalWallet.repository.TransactionRepository;
import com.furkan.digitalWallet.repository.WalletRepository;
import com.furkan.digitalWallet.request.DepositRequest;
import com.furkan.digitalWallet.request.WalletCreateRequest;
import com.furkan.digitalWallet.request.WithdrawRequest;
import com.furkan.digitalWallet.security.SecurityUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletServiceTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private WalletService walletService;

    private Customer customer;
    private Customer employee;
    private Wallet wallet;
    private WalletCreateRequest walletCreateRequest;
    private DepositRequest depositRequest;
    private WithdrawRequest withdrawRequest;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setId(1L);
        customer.setUsername("customer");
        customer.setRole(Role.CUSTOMER);

        employee = new Customer();
        employee.setId(2L);
        employee.setUsername("employee");
        employee.setRole(Role.EMPLOYEE);

        wallet = new Wallet();
        wallet.setId(1L);
        wallet.setCustomer(customer);
        wallet.setWalletName("Main Wallet");
        wallet.setCurrency(Currency.TRY);
        wallet.setBalance(BigDecimal.valueOf(1000));
        wallet.setUsableBalance(BigDecimal.valueOf(1000));
        wallet.setActiveForShopping(true);
        wallet.setActiveForWithdraw(true);

        walletCreateRequest = new WalletCreateRequest();
        walletCreateRequest.setWalletName("Test Wallet");
        walletCreateRequest.setCurrency(Currency.TRY);
        walletCreateRequest.setActiveForShopping(true);
        walletCreateRequest.setActiveForWithdraw(true);

        depositRequest = new DepositRequest();
        depositRequest.setWalletId(1L);
        depositRequest.setAmount(BigDecimal.valueOf(500));
        depositRequest.setOppositePartyType(OppositePartyType.IBAN);
        depositRequest.setSource("TR123456789");

        withdrawRequest = new WithdrawRequest();
        withdrawRequest.setWalletId(1L);
        withdrawRequest.setAmount(BigDecimal.valueOf(300));
        withdrawRequest.setOppositePartyType(OppositePartyType.IBAN);
        withdrawRequest.setDestination("TR987654321");
    }

    @Test
    void createWallet_ShouldCreateWallet_WhenValidRequest() {
        
        when(customerRepository.existsById(1L)).thenReturn(true);
        when(walletRepository.existsByCustomerIdAndWalletNameIgnoreCase(1L, "Test Wallet")).thenReturn(false);
        when(customerRepository.getReferenceById(1L)).thenReturn(customer);
        when(walletRepository.save(any(Wallet.class))).thenReturn(wallet);

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(() -> SecurityUtil.hasRole("EMPLOYEE")).thenReturn(false);

            
            Wallet result = walletService.createWallet(walletCreateRequest, customer);

            
            assertNotNull(result);
            verify(customerRepository).existsById(1L);
            verify(walletRepository).existsByCustomerIdAndWalletNameIgnoreCase(1L, "Test Wallet");
            verify(walletRepository).save(any(Wallet.class));
        }
    }

    @Test
    void createWallet_ShouldCreateWalletForSpecificCustomer_WhenEmployeeRequest() {
        
        walletCreateRequest.setCustomerId(1L);
        when(customerRepository.existsById(1L)).thenReturn(true);
        when(walletRepository.existsByCustomerIdAndWalletNameIgnoreCase(1L, "Test Wallet")).thenReturn(false);
        when(customerRepository.getReferenceById(1L)).thenReturn(customer);
        when(walletRepository.save(any(Wallet.class))).thenReturn(wallet);

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(() -> SecurityUtil.hasRole("EMPLOYEE")).thenReturn(true);

            
            Wallet result = walletService.createWallet(walletCreateRequest, employee);

            
            assertNotNull(result);
            verify(customerRepository).existsById(1L);
            verify(walletRepository).save(any(Wallet.class));
        }
    }

    @Test
    void createWallet_ShouldThrowNotFoundException_WhenCustomerNotExists() {
        
        when(customerRepository.existsById(1L)).thenReturn(false);

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(() -> SecurityUtil.hasRole("EMPLOYEE")).thenReturn(false);

            
            NotFoundException exception = assertThrows(NotFoundException.class,
                () -> walletService.createWallet(walletCreateRequest, customer));

            assertEquals("Müşteri bulunamadı: 1", exception.getMessage());
        }
    }

    @Test
    void createWallet_ShouldThrowBadRequestException_WhenWalletNameExists() {
        
        when(customerRepository.existsById(1L)).thenReturn(true);
        when(walletRepository.existsByCustomerIdAndWalletNameIgnoreCase(1L, "Test Wallet")).thenReturn(true);

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(() -> SecurityUtil.hasRole("EMPLOYEE")).thenReturn(false);

            
            BadRequestException exception = assertThrows(BadRequestException.class,
                () -> walletService.createWallet(walletCreateRequest, customer));

            assertEquals("Aynı isimde cüzdan mevcut", exception.getMessage());
        }
    }

    @Test
    void listWallets_ShouldReturnWallets_WhenNoFilters() {
        
        List<Wallet> wallets = Arrays.asList(wallet);
        when(walletRepository.findByCustomerId(1L)).thenReturn(wallets);

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(() -> SecurityUtil.hasRole("EMPLOYEE")).thenReturn(false);

            
            List<Wallet> result = walletService.listWallets(null, null, customer);

            
            assertNotNull(result);
            assertEquals(1, result.size());
            verify(walletRepository).findByCustomerId(1L);
        }
    }

    @Test
    void listWallets_ShouldReturnFilteredWallets_WhenCurrencyProvided() {
        
        List<Wallet> wallets = Arrays.asList(wallet);
        when(walletRepository.findByCustomerIdAndCurrency(1L, Currency.TRY)).thenReturn(wallets);

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(() -> SecurityUtil.hasRole("EMPLOYEE")).thenReturn(false);

            
            List<Wallet> result = walletService.listWallets(null, Currency.TRY, customer);

            
            assertNotNull(result);
            assertEquals(1, result.size());
            verify(walletRepository).findByCustomerIdAndCurrency(1L, Currency.TRY);
        }
    }

    @Test
    void listWallets_ShouldReturnWalletsForSpecificCustomer_WhenEmployeeRequest() {
        
        List<Wallet> wallets = Arrays.asList(wallet);
        when(walletRepository.findByCustomerId(1L)).thenReturn(wallets);

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(() -> SecurityUtil.hasRole("EMPLOYEE")).thenReturn(true);

            
            List<Wallet> result = walletService.listWallets(1L, null, employee);

            
            assertNotNull(result);
            assertEquals(1, result.size());
            verify(walletRepository).findByCustomerId(1L);
        }
    }

    @Test
    void deposit_ShouldCreateApprovedTransaction_WhenAmountUnder1000() {
        
        when(walletRepository.findById(1L)).thenReturn(Optional.of(wallet));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(walletRepository.save(any(Wallet.class))).thenReturn(wallet);

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(() -> SecurityUtil.hasRole("EMPLOYEE")).thenReturn(false);

            
            Transaction result = walletService.deposit(depositRequest, customer);

            
            assertNotNull(result);
            assertEquals(TransactionStatus.APPROVED, result.getStatus());
            assertEquals(TransactionType.DEPOSIT, result.getType());
            assertEquals(BigDecimal.valueOf(500), result.getAmount());
            verify(walletRepository).save(wallet);
            verify(transactionRepository).save(any(Transaction.class));
        }
    }

    @Test
    void deposit_ShouldCreatePendingTransaction_WhenAmountOver1000() {
        
        depositRequest.setAmount(BigDecimal.valueOf(1500));
        when(walletRepository.findById(1L)).thenReturn(Optional.of(wallet));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(walletRepository.save(any(Wallet.class))).thenReturn(wallet);

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(() -> SecurityUtil.hasRole("EMPLOYEE")).thenReturn(false);

            
            Transaction result = walletService.deposit(depositRequest, customer);

            
            assertNotNull(result);
            assertEquals(TransactionStatus.PENDING, result.getStatus());
            assertEquals(TransactionType.DEPOSIT, result.getType());
            assertEquals(BigDecimal.valueOf(1500), result.getAmount());
        }
    }

    @Test
    void withdraw_ShouldCreateApprovedTransaction_WhenValidRequest() {
        
        when(walletRepository.findById(1L)).thenReturn(Optional.of(wallet));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(walletRepository.save(any(Wallet.class))).thenReturn(wallet);

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(() -> SecurityUtil.hasRole("EMPLOYEE")).thenReturn(false);

            
            Transaction result = walletService.withdraw(withdrawRequest, customer);

            
            assertNotNull(result);
            assertEquals(TransactionStatus.APPROVED, result.getStatus());
            assertEquals(TransactionType.WITHDRAW, result.getType());
            assertEquals(BigDecimal.valueOf(300), result.getAmount());
        }
    }

    @Test
    void withdraw_ShouldCreatePendingTransaction_WhenAmountOver1000() {
        
        withdrawRequest.setAmount(BigDecimal.valueOf(1500));
        wallet.setUsableBalance(BigDecimal.valueOf(2000));
        when(walletRepository.findById(1L)).thenReturn(Optional.of(wallet));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(walletRepository.save(any(Wallet.class))).thenReturn(wallet);

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(() -> SecurityUtil.hasRole("EMPLOYEE")).thenReturn(false);

            
            Transaction result = walletService.withdraw(withdrawRequest, customer);

            
            assertNotNull(result);
            assertEquals(TransactionStatus.PENDING, result.getStatus());
        }
    }

    @Test
    void withdraw_ShouldThrowBadRequestException_WhenWalletNotActiveForShopping() {
        
        withdrawRequest.setOppositePartyType(OppositePartyType.PAYMENT);
        wallet.setActiveForShopping(false);
        when(walletRepository.findById(1L)).thenReturn(Optional.of(wallet));

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(() -> SecurityUtil.hasRole("EMPLOYEE")).thenReturn(false);

            
            BadRequestException exception = assertThrows(BadRequestException.class,
                () -> walletService.withdraw(withdrawRequest, customer));

            assertEquals("Cüzdan alışverişe kapalı", exception.getMessage());
        }
    }

    @Test
    void withdraw_ShouldThrowBadRequestException_WhenWalletNotActiveForWithdraw() {
        
        wallet.setActiveForWithdraw(false);
        when(walletRepository.findById(1L)).thenReturn(Optional.of(wallet));

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(() -> SecurityUtil.hasRole("EMPLOYEE")).thenReturn(false);

            
            BadRequestException exception = assertThrows(BadRequestException.class,
                () -> walletService.withdraw(withdrawRequest, customer));

            assertEquals("Cüzdan çekime kapalı", exception.getMessage());
        }
    }

    @Test
    void withdraw_ShouldThrowBadRequestException_WhenInsufficientBalance() {
        
        wallet.setUsableBalance(BigDecimal.valueOf(100));
        when(walletRepository.findById(1L)).thenReturn(Optional.of(wallet));

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(() -> SecurityUtil.hasRole("EMPLOYEE")).thenReturn(false);

            
            BadRequestException exception = assertThrows(BadRequestException.class,
                () -> walletService.withdraw(withdrawRequest, customer));

            assertEquals("Yetersiz kullanılabilir bakiye", exception.getMessage());
        }
    }

    @Test
    void listTransactions_ShouldReturnTransactions_WhenValidWalletId() {
        
        Transaction transaction = new Transaction();
        transaction.setId(1L);
        List<Transaction> transactions = Arrays.asList(transaction);

        when(walletRepository.findById(1L)).thenReturn(Optional.of(wallet));
        when(transactionRepository.findByWalletOrderByCreatedAtDesc(wallet)).thenReturn(transactions);

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(() -> SecurityUtil.hasRole("EMPLOYEE")).thenReturn(false);

            
            List<Transaction> result = walletService.listTransactions(1L, customer);

            
            assertNotNull(result);
            assertEquals(1, result.size());
            verify(transactionRepository).findByWalletOrderByCreatedAtDesc(wallet);
        }
    }

    @Test
    void getWalletForAccess_ShouldReturnWallet_WhenCustomerOwnsWallet() {
        
        when(walletRepository.findById(1L)).thenReturn(Optional.of(wallet));

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(() -> SecurityUtil.hasRole("EMPLOYEE")).thenReturn(false);

            
            Wallet result = walletService.getWalletForAccess(1L, customer);

            
            assertNotNull(result);
            assertEquals(wallet.getId(), result.getId());
        }
    }

    @Test
    void getWalletForAccess_ShouldReturnWallet_WhenEmployee() {
        
        when(walletRepository.findById(1L)).thenReturn(Optional.of(wallet));

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(() -> SecurityUtil.hasRole("EMPLOYEE")).thenReturn(true);

            
            Wallet result = walletService.getWalletForAccess(1L, employee);

            
            assertNotNull(result);
            assertEquals(wallet.getId(), result.getId());
        }
    }

    @Test
    void getWalletForAccess_ShouldThrowNotFoundException_WhenWalletNotExists() {
        
        when(walletRepository.findById(1L)).thenReturn(Optional.empty());

        
        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> walletService.getWalletForAccess(1L, customer));

        assertEquals("Cüzdan bulunamadı", exception.getMessage());
    }

    @Test
    void getWalletForAccess_ShouldThrowBadRequestException_WhenCustomerDoesNotOwnWallet() {
        
        Customer otherCustomer = new Customer();
        otherCustomer.setId(2L);
        when(walletRepository.findById(1L)).thenReturn(Optional.of(wallet));

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(() -> SecurityUtil.hasRole("EMPLOYEE")).thenReturn(false);

            
            BadRequestException exception = assertThrows(BadRequestException.class,
                () -> walletService.getWalletForAccess(1L, otherCustomer));

            assertEquals("Bu cüzdana erişim yetkiniz yok", exception.getMessage());
        }
    }
}
