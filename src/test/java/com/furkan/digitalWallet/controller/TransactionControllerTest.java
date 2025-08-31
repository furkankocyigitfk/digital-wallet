package com.furkan.digitalWallet.controller;

import com.furkan.digitalWallet.entity.Customer;
import com.furkan.digitalWallet.entity.Transaction;
import com.furkan.digitalWallet.enums.Role;
import com.furkan.digitalWallet.request.DepositRequest;
import com.furkan.digitalWallet.request.TransactionDecisionRequest;
import com.furkan.digitalWallet.request.WithdrawRequest;
import com.furkan.digitalWallet.security.SecurityUtil;
import com.furkan.digitalWallet.service.CustomerService;
import com.furkan.digitalWallet.service.TransactionService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionControllerTest {

    @Mock
    private TransactionService transactionService;

    @Mock
    private CustomerService customerService;

    @InjectMocks
    private TransactionController transactionController;

    private MockedStatic<SecurityUtil> securityUtilMockedStatic;
    private Customer customer;
    private DepositRequest depositRequest;
    private WithdrawRequest withdrawRequest;
    private TransactionDecisionRequest decisionRequest;
    private Transaction transaction;

    @BeforeEach
    void setUp() {
        securityUtilMockedStatic = mockStatic(SecurityUtil.class);

        customer = new Customer();
        customer.setUsername("testuser");
        customer.setRole(Role.CUSTOMER);

        depositRequest = new DepositRequest();
        withdrawRequest = new WithdrawRequest();
        decisionRequest = new TransactionDecisionRequest();

        transaction = new Transaction();
        transaction.setId(1L);
    }

    @AfterEach
    void tearDown() {
        securityUtilMockedStatic.close();
    }

    @Test
    void deposit_Successful_ReturnsTransaction() {
        securityUtilMockedStatic.when(SecurityUtil::currentUsername).thenReturn("testuser");
        when(customerService.getByUsername("testuser")).thenReturn(customer);
        when(transactionService.deposit(any(DepositRequest.class), any(Customer.class))).thenReturn(transaction);

        ResponseEntity<Transaction> response = transactionController.deposit(depositRequest);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(transaction, response.getBody());

        verify(customerService).getByUsername("testuser");
        verify(transactionService).deposit(depositRequest, customer);
        verifyNoMoreInteractions(customerService, transactionService);
    }

    @Test
    void deposit_CustomerNotFound_ThrowsException() {
        securityUtilMockedStatic.when(SecurityUtil::currentUsername).thenReturn("testuser");
        when(customerService.getByUsername("testuser")).thenThrow(new RuntimeException("Customer not found"));

        assertThrows(RuntimeException.class, () -> transactionController.deposit(depositRequest));

        verify(customerService).getByUsername("testuser");
        verifyNoInteractions(transactionService);
    }

    @Test
    void withdraw_Successful_ReturnsTransaction() {
        securityUtilMockedStatic.when(SecurityUtil::currentUsername).thenReturn("testuser");
        when(customerService.getByUsername("testuser")).thenReturn(customer);
        when(transactionService.withdraw(any(WithdrawRequest.class), any(Customer.class))).thenReturn(transaction);

        ResponseEntity<Transaction> response = transactionController.withdraw(withdrawRequest);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(transaction, response.getBody());

        verify(customerService).getByUsername("testuser");
        verify(transactionService).withdraw(withdrawRequest, customer);
        verifyNoMoreInteractions(customerService, transactionService);
    }

    @Test
    void withdraw_CustomerNotFound_ThrowsException() {
        securityUtilMockedStatic.when(SecurityUtil::currentUsername).thenReturn("testuser");
        when(customerService.getByUsername("testuser")).thenThrow(new RuntimeException("Customer not found"));

        assertThrows(RuntimeException.class, () -> transactionController.withdraw(withdrawRequest));

        verify(customerService).getByUsername("testuser");
        verifyNoInteractions(transactionService);
    }

    @Test
    void decide_Successful_ReturnsTransaction() {
        when(transactionService.decide(eq(1L), any(TransactionDecisionRequest.class))).thenReturn(transaction);

        ResponseEntity<Transaction> response = transactionController.decide(1L, decisionRequest);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(transaction, response.getBody());

        verify(transactionService).decide(1L, decisionRequest);
        verifyNoMoreInteractions(transactionService);
    }
}