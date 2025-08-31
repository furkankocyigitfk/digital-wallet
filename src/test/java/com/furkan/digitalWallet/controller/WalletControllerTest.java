package com.furkan.digitalWallet.controller;

import com.furkan.digitalWallet.entity.Customer;
import com.furkan.digitalWallet.entity.Transaction;
import com.furkan.digitalWallet.entity.Wallet;
import com.furkan.digitalWallet.enums.Currency;
import com.furkan.digitalWallet.enums.Role;
import com.furkan.digitalWallet.request.WalletCreateRequest;
import com.furkan.digitalWallet.security.SecurityUtil;
import com.furkan.digitalWallet.service.CustomerService;
import com.furkan.digitalWallet.service.TransactionService;
import com.furkan.digitalWallet.service.WalletService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletControllerTest {

    @Mock
    private WalletService walletService;

    @Mock
    private CustomerService customerService;

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private WalletController walletController;

    private MockedStatic<SecurityUtil> securityUtilMockedStatic;
    private Customer customer;
    private WalletCreateRequest walletCreateRequest;
    private Wallet wallet;
    private Transaction transaction;

    @BeforeEach
    void setUp() {
        securityUtilMockedStatic = mockStatic(SecurityUtil.class);

        customer = new Customer();
        customer.setUsername("testuser");
        customer.setRole(Role.CUSTOMER);

        walletCreateRequest = new WalletCreateRequest();
        wallet = new Wallet();
        wallet.setId(1L);

        transaction = new Transaction();
        transaction.setId(1L);
    }

    @AfterEach
    void tearDown() {
        securityUtilMockedStatic.close();
    }

    @Test
    void create_Successful_ReturnsWallet() {
        securityUtilMockedStatic.when(SecurityUtil::currentUsername).thenReturn("testuser");
        when(customerService.getByUsername("testuser")).thenReturn(customer);
        when(walletService.createWallet(any(WalletCreateRequest.class), any(Customer.class))).thenReturn(wallet);

        ResponseEntity<Wallet> response = walletController.create(walletCreateRequest);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(wallet, response.getBody());

        verify(customerService).getByUsername("testuser");
        verify(walletService).createWallet(walletCreateRequest, customer);
        verifyNoMoreInteractions(customerService, walletService);
    }

    @Test
    void create_CustomerNotFound_ThrowsException() {
        securityUtilMockedStatic.when(SecurityUtil::currentUsername).thenReturn("testuser");
        when(customerService.getByUsername("testuser")).thenThrow(new RuntimeException("Customer not found"));

        assertThrows(RuntimeException.class, () -> walletController.create(walletCreateRequest));

        verify(customerService).getByUsername("testuser");
        verifyNoInteractions(walletService);
    }

    @Test
    void list_SuccessfulWithCustomerIdAndCurrency_ReturnsWallets() {
        List<Wallet> wallets = Collections.singletonList(wallet);
        securityUtilMockedStatic.when(SecurityUtil::currentUsername).thenReturn("testuser");
        when(customerService.getByUsername("testuser")).thenReturn(customer);
        when(walletService.listWallets(eq(1L), eq(Currency.TRY), any(Customer.class))).thenReturn(wallets);

        ResponseEntity<List<Wallet>> response = walletController.list(1L, Currency.TRY);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(wallets, response.getBody());

        verify(customerService).getByUsername("testuser");
        verify(walletService).listWallets(1L, Currency.TRY, customer);
        verifyNoMoreInteractions(customerService, walletService);
    }

    @Test
    void list_SuccessfulWithoutParameters_ReturnsWallets() {
        List<Wallet> wallets = Collections.singletonList(wallet);
        securityUtilMockedStatic.when(SecurityUtil::currentUsername).thenReturn("testuser");
        when(customerService.getByUsername("testuser")).thenReturn(customer);
        when(walletService.listWallets(isNull(), isNull(), any(Customer.class))).thenReturn(wallets);

        ResponseEntity<List<Wallet>> response = walletController.list(null, null);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(wallets, response.getBody());

        verify(customerService).getByUsername("testuser");
        verify(walletService).listWallets(null, null, customer);
        verifyNoMoreInteractions(customerService, walletService);
    }

    @Test
    void list_CustomerNotFound_ThrowsException() {
        securityUtilMockedStatic.when(SecurityUtil::currentUsername).thenReturn("testuser");
        when(customerService.getByUsername("testuser")).thenThrow(new RuntimeException("Customer not found"));

        assertThrows(RuntimeException.class, () -> walletController.list(1L, Currency.TRY));

        verify(customerService).getByUsername("testuser");
        verifyNoInteractions(walletService);
    }

    @Test
    void listTransactions_Successful_ReturnsTransactions() {
        List<Transaction> transactions = Collections.singletonList(transaction);
        securityUtilMockedStatic.when(SecurityUtil::currentUsername).thenReturn("testuser");
        when(customerService.getByUsername("testuser")).thenReturn(customer);
        when(transactionService.listTransactions(eq(1L), any(Customer.class))).thenReturn(transactions);

        ResponseEntity<List<Transaction>> response = walletController.listTransactions(1L);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(transactions, response.getBody());

        verify(customerService).getByUsername("testuser");
        verify(transactionService).listTransactions(1L, customer);
        verifyNoMoreInteractions(customerService, transactionService);
    }

    @Test
    void listTransactions_CustomerNotFound_ThrowsException() {
        securityUtilMockedStatic.when(SecurityUtil::currentUsername).thenReturn("testuser");
        when(customerService.getByUsername("testuser")).thenThrow(new RuntimeException("Customer not found"));

        assertThrows(RuntimeException.class, () -> walletController.listTransactions(1L));

        verify(customerService).getByUsername("testuser");
        verifyNoInteractions(walletService, transactionService);
    }
}