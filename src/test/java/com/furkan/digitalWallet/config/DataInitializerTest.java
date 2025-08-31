package com.furkan.digitalWallet.config;

import com.furkan.digitalWallet.entity.Customer;
import com.furkan.digitalWallet.entity.Wallet;
import com.furkan.digitalWallet.enums.Currency;
import com.furkan.digitalWallet.enums.Role;
import com.furkan.digitalWallet.repository.CustomerRepository;
import com.furkan.digitalWallet.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataInitializerTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private DataInitializer dataInitializer;

    @BeforeEach
    void setUp() {
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-password");
    }


    @Test
    void init_ShouldCreateAllCustomers_WhenNoCustomersExist() {
        dataInitializer.init();

        ArgumentCaptor<Customer> customerCaptor = ArgumentCaptor.forClass(Customer.class);
        verify(customerRepository, times(5)).save(customerCaptor.capture());

        List<Customer> capturedCustomers = customerCaptor.getAllValues();

        Customer employee = capturedCustomers.get(0);
        assertEquals("Ayşe", employee.getName());
        assertEquals("Yılmaz", employee.getSurname());
        assertEquals("11111111111", employee.getTckn());
        assertEquals("employee", employee.getUsername());
        assertEquals("encoded-password", employee.getPassword());
        assertEquals(Role.EMPLOYEE, employee.getRole());


        Customer customer1 = capturedCustomers.get(1);
        assertEquals("Ali", customer1.getName());
        assertEquals("Kaya", customer1.getSurname());
        assertEquals("22222222222", customer1.getTckn());
        assertEquals("customer1", customer1.getUsername());
        assertEquals("encoded-password", customer1.getPassword());
        assertEquals(Role.CUSTOMER, customer1.getRole());


        Customer customer2 = capturedCustomers.get(2);
        assertEquals("Mehmet", customer2.getName());
        assertEquals("Özkan", customer2.getSurname());
        assertEquals("33333333333", customer2.getTckn());
        assertEquals("customer2", customer2.getUsername());
        assertEquals("encoded-password", customer2.getPassword());
        assertEquals(Role.CUSTOMER, customer2.getRole());


        Customer customer3 = capturedCustomers.get(3);
        assertEquals("Fatma", customer3.getName());
        assertEquals("Demir", customer3.getSurname());
        assertEquals("44444444444", customer3.getTckn());
        assertEquals("customer3", customer3.getUsername());
        assertEquals("encoded-password", customer3.getPassword());
        assertEquals(Role.CUSTOMER, customer3.getRole());


        Customer customer4 = capturedCustomers.get(4);
        assertEquals("Ahmet", customer4.getName());
        assertEquals("Çelik", customer4.getSurname());
        assertEquals("55555555555", customer4.getTckn());
        assertEquals("customer4", customer4.getUsername());
        assertEquals("encoded-password", customer4.getPassword());
        assertEquals(Role.CUSTOMER, customer4.getRole());
    }

    @Test
    void init_ShouldEncodePasswords_WhenCreatingCustomers() {
        dataInitializer.init();

        verify(passwordEncoder).encode("employee123");
        verify(passwordEncoder).encode("customer123");
        verify(passwordEncoder).encode("password123");
        verify(passwordEncoder).encode("test123");
        verify(passwordEncoder).encode("demo123");
    }

    @Test
    void init_ShouldCreateWallets_WhenCreatingCustomers() {
        dataInitializer.init();


        ArgumentCaptor<Wallet> walletCaptor = ArgumentCaptor.forClass(Wallet.class);
        verify(walletRepository, times(10)).save(walletCaptor.capture());

        List<Wallet> capturedWallets = walletCaptor.getAllValues();
        Wallet customer1TryWallet = capturedWallets.get(0);
        assertEquals("Ana TRY Cüzdan", customer1TryWallet.getWalletName());
        assertEquals(Currency.TRY, customer1TryWallet.getCurrency());
        assertTrue(customer1TryWallet.getActiveForShopping());
        assertTrue(customer1TryWallet.getActiveForWithdraw());
        assertEquals(new BigDecimal("10000.00"), customer1TryWallet.getBalance());
        assertEquals(new BigDecimal("10000.00"), customer1TryWallet.getUsableBalance());

        Wallet customer1UsdWallet = capturedWallets.get(1);
        assertEquals("USD Cüzdan", customer1UsdWallet.getWalletName());
        assertEquals(Currency.USD, customer1UsdWallet.getCurrency());
        assertEquals(new BigDecimal("1000.00"), customer1UsdWallet.getBalance());
        assertEquals(new BigDecimal("1000.00"), customer1UsdWallet.getUsableBalance());

        Wallet customer1EurWallet = capturedWallets.get(2);
        assertEquals("EUR Cüzdan", customer1EurWallet.getWalletName());
        assertEquals(Currency.EUR, customer1EurWallet.getCurrency());
        assertEquals(new BigDecimal("800.00"), customer1EurWallet.getBalance());
        assertEquals(new BigDecimal("800.00"), customer1EurWallet.getUsableBalance());
    }

    @Test
    void init_ShouldCreateCustomer4WithOnlyTryWallet() {
        dataInitializer.init();

        ArgumentCaptor<Wallet> walletCaptor = ArgumentCaptor.forClass(Wallet.class);
        verify(walletRepository, times(10)).save(walletCaptor.capture());

        List<Wallet> capturedWallets = walletCaptor.getAllValues();

        Wallet customer4TryWallet = capturedWallets.get(9);
        assertEquals("Ana TRY Cüzdan", customer4TryWallet.getWalletName());
        assertEquals(Currency.TRY, customer4TryWallet.getCurrency());
        assertTrue(customer4TryWallet.getActiveForShopping());
        assertTrue(customer4TryWallet.getActiveForWithdraw());
        assertEquals(new BigDecimal("5000.00"), customer4TryWallet.getBalance());
        assertEquals(new BigDecimal("5000.00"), customer4TryWallet.getUsableBalance());
        assertNotNull(customer4TryWallet.getCreatedAt());
    }

    @Test
    void init_ShouldVerifyWalletProperties_ForAllWallets() {
        dataInitializer.init();

        ArgumentCaptor<Wallet> walletCaptor = ArgumentCaptor.forClass(Wallet.class);
        verify(walletRepository, times(10)).save(walletCaptor.capture());

        List<Wallet> capturedWallets = walletCaptor.getAllValues();

        for (Wallet wallet : capturedWallets) {
            assertNotNull(wallet.getCustomer());
            assertNotNull(wallet.getWalletName());
            assertNotNull(wallet.getCurrency());
            assertTrue(wallet.getActiveForShopping());
            assertTrue(wallet.getActiveForWithdraw());
            assertNotNull(wallet.getBalance());
            assertNotNull(wallet.getUsableBalance());
            assertNotNull(wallet.getCreatedAt());

            assertTrue(wallet.getBalance().compareTo(BigDecimal.ZERO) >= 0);
            assertTrue(wallet.getUsableBalance().compareTo(BigDecimal.ZERO) >= 0);
        }
    }

    @Test
    void init_ShouldCreateWalletsWithCorrectCustomerAssociation() {
        dataInitializer.init();

        ArgumentCaptor<Customer> customerCaptor = ArgumentCaptor.forClass(Customer.class);
        ArgumentCaptor<Wallet> walletCaptor = ArgumentCaptor.forClass(Wallet.class);

        verify(customerRepository, times(5)).save(customerCaptor.capture());
        verify(walletRepository, times(10)).save(walletCaptor.capture());

        List<Customer> capturedCustomers = customerCaptor.getAllValues();
        List<Wallet> capturedWallets = walletCaptor.getAllValues();

        Customer customer1 = capturedCustomers.get(1);
        for (int i = 0; i < 3; i++) {
            assertEquals(customer1, capturedWallets.get(i).getCustomer());
        }

        Customer customer2 = capturedCustomers.get(2);
        for (int i = 3; i < 6; i++) {
            assertEquals(customer2, capturedWallets.get(i).getCustomer());
        }

        Customer customer3 = capturedCustomers.get(3);
        for (int i = 6; i < 9; i++) {
            assertEquals(customer3, capturedWallets.get(i).getCustomer());
        }

        Customer customer4 = capturedCustomers.get(4);
        assertEquals(customer4, capturedWallets.get(9).getCustomer());
    }

    @Test
    void init_ShouldVerifyCorrectNumberOfWalletsPerCurrency() {
        dataInitializer.init();
        ArgumentCaptor<Wallet> walletCaptor = ArgumentCaptor.forClass(Wallet.class);
        verify(walletRepository, times(10)).save(walletCaptor.capture());

        List<Wallet> capturedWallets = walletCaptor.getAllValues();

        long tryWallets = capturedWallets.stream()
                .filter(w -> w.getCurrency() == Currency.TRY)
                .count();

        long usdWallets = capturedWallets.stream()
                .filter(w -> w.getCurrency() == Currency.USD)
                .count();

        long eurWallets = capturedWallets.stream()
                .filter(w -> w.getCurrency() == Currency.EUR)
                .count();

        assertEquals(4, tryWallets);
        assertEquals(3, usdWallets);
        assertEquals(3, eurWallets);
    }

    @Test
    void init_ShouldVerifyPasswordEncodingCalledCorrectly() {
        when(passwordEncoder.encode("employee123")).thenReturn("encoded-employee123");
        when(passwordEncoder.encode("customer123")).thenReturn("encoded-customer123");
        when(passwordEncoder.encode("password123")).thenReturn("encoded-password123");
        when(passwordEncoder.encode("test123")).thenReturn("encoded-test123");
        when(passwordEncoder.encode("demo123")).thenReturn("encoded-demo123");

        dataInitializer.init();

        ArgumentCaptor<Customer> customerCaptor = ArgumentCaptor.forClass(Customer.class);
        verify(customerRepository, times(5)).save(customerCaptor.capture());

        List<Customer> capturedCustomers = customerCaptor.getAllValues();

        assertEquals("encoded-employee123", capturedCustomers.get(0).getPassword());
        assertEquals("encoded-customer123", capturedCustomers.get(1).getPassword());
        assertEquals("encoded-password123", capturedCustomers.get(2).getPassword());
        assertEquals("encoded-test123", capturedCustomers.get(3).getPassword());
        assertEquals("encoded-demo123", capturedCustomers.get(4).getPassword());
    }
}
