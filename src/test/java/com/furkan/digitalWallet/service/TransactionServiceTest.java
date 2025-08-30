package com.furkan.digitalWallet.service;

import com.furkan.digitalWallet.entity.Transaction;
import com.furkan.digitalWallet.entity.Wallet;
import com.furkan.digitalWallet.enums.TransactionStatus;
import com.furkan.digitalWallet.enums.TransactionType;
import com.furkan.digitalWallet.exception.BadRequestException;
import com.furkan.digitalWallet.exception.NotFoundException;
import com.furkan.digitalWallet.repository.TransactionRepository;
import com.furkan.digitalWallet.repository.WalletRepository;
import com.furkan.digitalWallet.request.TransactionDecisionRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private WalletRepository walletRepository;

    @InjectMocks
    private TransactionService transactionService;

    private Transaction transaction;
    private Wallet wallet;
    private TransactionDecisionRequest decisionRequest;

    @BeforeEach
    void setUp() {
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
    }

    @Test
    void decide_ShouldApproveDepositTransaction_WhenValidRequest() {
        // Given
        transaction.setType(TransactionType.DEPOSIT);
        decisionRequest.setStatus(TransactionStatus.APPROVED);

        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);
        when(walletRepository.save(any(Wallet.class))).thenReturn(wallet);

        // When
        Transaction result = transactionService.decide(1L, decisionRequest);

        // Then
        assertNotNull(result);
        assertEquals(TransactionStatus.APPROVED, result.getStatus());
        assertNotNull(result.getUpdatedAt());

        // Verify wallet balance increased for approved deposit
        assertEquals(BigDecimal.valueOf(1300), wallet.getUsableBalance()); // 800 + 500

        verify(transactionRepository).findById(1L);
        verify(transactionRepository).save(transaction);
        verify(walletRepository).save(wallet);
    }

    @Test
    void decide_ShouldApproveWithdrawTransaction_WhenValidRequest() {
        // Given
        transaction.setType(TransactionType.WITHDRAW);
        decisionRequest.setStatus(TransactionStatus.APPROVED);

        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);
        when(walletRepository.save(any(Wallet.class))).thenReturn(wallet);

        // When
        Transaction result = transactionService.decide(1L, decisionRequest);

        // Then
        assertNotNull(result);
        assertEquals(TransactionStatus.APPROVED, result.getStatus());
        assertNotNull(result.getUpdatedAt());

        // Verify wallet balance decreased for approved withdraw
        assertEquals(BigDecimal.valueOf(500), wallet.getBalance()); // 1000 - 500

        verify(transactionRepository).save(transaction);
        verify(walletRepository).save(wallet);
    }

    @Test
    void decide_ShouldDenyDepositTransaction_WhenValidRequest() {
        // Given
        transaction.setType(TransactionType.DEPOSIT);
        decisionRequest.setStatus(TransactionStatus.DENIED);

        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);
        when(walletRepository.save(any(Wallet.class))).thenReturn(wallet);

        // When
        Transaction result = transactionService.decide(1L, decisionRequest);

        // Then
        assertNotNull(result);
        assertEquals(TransactionStatus.DENIED, result.getStatus());
        assertNotNull(result.getUpdatedAt());

        // Verify wallet balance reverted for denied deposit
        assertEquals(BigDecimal.valueOf(500), wallet.getBalance()); // 1000 - 500

        verify(transactionRepository).save(transaction);
        verify(walletRepository).save(wallet);
    }

    @Test
    void decide_ShouldDenyWithdrawTransaction_WhenValidRequest() {
        // Given
        transaction.setType(TransactionType.WITHDRAW);
        decisionRequest.setStatus(TransactionStatus.DENIED);

        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);
        when(walletRepository.save(any(Wallet.class))).thenReturn(wallet);

        // When
        Transaction result = transactionService.decide(1L, decisionRequest);

        // Then
        assertNotNull(result);
        assertEquals(TransactionStatus.DENIED, result.getStatus());
        assertNotNull(result.getUpdatedAt());

        // Verify usable balance restored for denied withdraw
        assertEquals(BigDecimal.valueOf(1300), wallet.getUsableBalance()); // 800 + 500

        verify(transactionRepository).save(transaction);
        verify(walletRepository).save(wallet);
    }

    @Test
    void decide_ShouldThrowNotFoundException_WhenTransactionNotExists() {
        // Given
        when(transactionRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> transactionService.decide(1L, decisionRequest));

        assertEquals("İşlem bulunamadı", exception.getMessage());
        verify(transactionRepository).findById(1L);
        verifyNoMoreInteractions(transactionRepository, walletRepository);
    }

    @Test
    void decide_ShouldThrowBadRequestException_WhenTransactionNotPending() {
        // Given
        transaction.setStatus(TransactionStatus.APPROVED);
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));

        // When & Then
        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> transactionService.decide(1L, decisionRequest));

        assertEquals("Sadece bekleyen işlemler onay/ret edilebilir", exception.getMessage());
        verify(transactionRepository).findById(1L);
        verifyNoMoreInteractions(transactionRepository, walletRepository);
    }

    @Test
    void decide_ShouldThrowBadRequestException_WhenInvalidStatus() {
        // Given
        decisionRequest.setStatus(TransactionStatus.PENDING);
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));

        // When & Then
        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> transactionService.decide(1L, decisionRequest));

        assertEquals("Geçersiz durum", exception.getMessage());
        verify(transactionRepository).findById(1L);
        verifyNoMoreInteractions(transactionRepository, walletRepository);
    }
}
