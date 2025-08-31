package com.furkan.digitalWallet.service;

import com.furkan.digitalWallet.entity.Customer;
import com.furkan.digitalWallet.entity.Transaction;
import com.furkan.digitalWallet.entity.Wallet;
import com.furkan.digitalWallet.enums.TransactionStatus;
import com.furkan.digitalWallet.enums.TransactionType;
import com.furkan.digitalWallet.exception.BadRequestException;
import com.furkan.digitalWallet.exception.NotFoundException;
import com.furkan.digitalWallet.repository.TransactionRepository;
import com.furkan.digitalWallet.request.DepositRequest;
import com.furkan.digitalWallet.request.TransactionDecisionRequest;
import com.furkan.digitalWallet.request.WithdrawRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private WalletService walletService;

    @InjectMocks
    private TransactionService transactionService;

    private Transaction transaction;
    private Wallet wallet;
    private Customer customer;
    private TransactionDecisionRequest decisionRequest;
    private DepositRequest depositRequest;
    private WithdrawRequest withdrawRequest;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setId(1L);
        customer.setUsername("testuser");

        wallet = new Wallet();
        wallet.setId(1L);
        wallet.setBalance(BigDecimal.valueOf(1000));
        wallet.setUsableBalance(BigDecimal.valueOf(800));

        transaction = new Transaction();
        transaction.setId(1L);
        transaction.setWallet(wallet);
        transaction.setAmount(BigDecimal.valueOf(500));
        transaction.setStatus(TransactionStatus.PENDING);
        transaction.setCreatedAt(LocalDateTime.now());

        decisionRequest = new TransactionDecisionRequest();
        depositRequest = new DepositRequest();
        withdrawRequest = new WithdrawRequest();
    }

    @Test
    void decide_ShouldApproveDepositTransaction_WhenValidRequest() {
        transaction.setType(TransactionType.DEPOSIT);
        decisionRequest.setStatus(TransactionStatus.APPROVED);

        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);
        when(walletService.processTransactionDecision(transaction, TransactionStatus.APPROVED)).thenReturn(wallet);

        Transaction result = transactionService.decide(1L, decisionRequest);

        assertNotNull(result);
        assertEquals(TransactionStatus.APPROVED, result.getStatus());
        assertNotNull(result.getUpdatedAt());

        verify(transactionRepository).findById(1L);
        verify(transactionRepository).save(transaction);
        verify(walletService).processTransactionDecision(transaction, TransactionStatus.APPROVED);
    }

    @Test
    void decide_ShouldApproveWithdrawTransaction_WhenValidRequest() {
        transaction.setType(TransactionType.WITHDRAW);
        decisionRequest.setStatus(TransactionStatus.APPROVED);

        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);
        when(walletService.processTransactionDecision(transaction, TransactionStatus.APPROVED)).thenReturn(wallet);

        Transaction result = transactionService.decide(1L, decisionRequest);

        assertNotNull(result);
        assertEquals(TransactionStatus.APPROVED, result.getStatus());
        assertNotNull(result.getUpdatedAt());

        verify(transactionRepository).save(transaction);
        verify(walletService).processTransactionDecision(transaction, TransactionStatus.APPROVED);
    }

    @Test
    void decide_ShouldDenyDepositTransaction_WhenValidRequest() {
        transaction.setType(TransactionType.DEPOSIT);
        decisionRequest.setStatus(TransactionStatus.DENIED);

        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);
        when(walletService.processTransactionDecision(transaction, TransactionStatus.DENIED)).thenReturn(wallet);

        Transaction result = transactionService.decide(1L, decisionRequest);

        assertNotNull(result);
        assertEquals(TransactionStatus.DENIED, result.getStatus());
        assertNotNull(result.getUpdatedAt());

        verify(transactionRepository).save(transaction);
        verify(walletService).processTransactionDecision(transaction, TransactionStatus.DENIED);
    }

    @Test
    void decide_ShouldDenyWithdrawTransaction_WhenValidRequest() {
        transaction.setType(TransactionType.WITHDRAW);
        decisionRequest.setStatus(TransactionStatus.DENIED);

        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);
        when(walletService.processTransactionDecision(transaction, TransactionStatus.DENIED)).thenReturn(wallet);

        Transaction result = transactionService.decide(1L, decisionRequest);

        assertNotNull(result);
        assertEquals(TransactionStatus.DENIED, result.getStatus());
        assertNotNull(result.getUpdatedAt());

        verify(transactionRepository).save(transaction);
        verify(walletService).processTransactionDecision(transaction, TransactionStatus.DENIED);
    }

    @Test
    void decide_ShouldThrowNotFoundException_WhenTransactionNotExists() {
        when(transactionRepository.findById(1L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> transactionService.decide(1L, decisionRequest));

        assertEquals("İşlem bulunamadı", exception.getMessage());
        verify(transactionRepository).findById(1L);
        verifyNoMoreInteractions(transactionRepository, walletService);
    }

    @Test
    void decide_ShouldThrowBadRequestException_WhenTransactionNotPending() {
        transaction.setStatus(TransactionStatus.APPROVED);
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> transactionService.decide(1L, decisionRequest));

        assertEquals("Sadece bekleyen işlemler onay/ret edilebilir", exception.getMessage());
        verify(transactionRepository).findById(1L);
        verifyNoMoreInteractions(transactionRepository, walletService);
    }

    @Test
    void decide_ShouldThrowBadRequestException_WhenInvalidStatus() {
        decisionRequest.setStatus(TransactionStatus.PENDING);
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> transactionService.decide(1L, decisionRequest));

        assertEquals("Geçersiz durum", exception.getMessage());
        verify(transactionRepository).findById(1L);
    }

    @Test
    void deposit_ShouldCreateTransaction_WhenValidRequest() {
        depositRequest.setAmount(BigDecimal.ONE);
        when(walletService.processDeposit(depositRequest, customer)).thenReturn(wallet);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        Transaction result = transactionService.deposit(depositRequest, customer);

        assertNotNull(result);
        verify(walletService).processDeposit(depositRequest, customer);
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void withdraw_ShouldCreateTransaction_WhenValidRequest() {
        withdrawRequest.setAmount(BigDecimal.ONE);
        when(walletService.processWithdraw(withdrawRequest, customer)).thenReturn(wallet);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        Transaction result = transactionService.withdraw(withdrawRequest, customer);

        assertNotNull(result);
        verify(walletService).processWithdraw(withdrawRequest, customer);
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void listTransactions_ShouldReturnTransactions_WhenValidWalletId() {
        List<Transaction> transactions = Collections.singletonList(transaction);
        when(walletService.getWalletForAccess(1L, customer)).thenReturn(wallet);
        when(transactionRepository.findByWalletOrderByCreatedAtDesc(wallet)).thenReturn(transactions);

        List<Transaction> result = transactionService.listTransactions(1L, customer);

        assertEquals(transactions, result);
        verify(walletService).getWalletForAccess(1L, customer);
        verify(transactionRepository).findByWalletOrderByCreatedAtDesc(wallet);
    }

    @Test
    void findById_ShouldReturnTransaction_WhenExists() {
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));

        Transaction result = transactionService.findById(1L);

        assertEquals(transaction, result);
        verify(transactionRepository).findById(1L);
    }

    @Test
    void findById_ShouldThrowNotFoundException_WhenNotExists() {
        when(transactionRepository.findById(1L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> transactionService.findById(1L));

        assertEquals("İşlem bulunamadı", exception.getMessage());
        verify(transactionRepository).findById(1L);
    }
}
