package com.furkan.digitalWallet.service;

import com.furkan.digitalWallet.entity.Customer;
import com.furkan.digitalWallet.entity.Transaction;
import com.furkan.digitalWallet.entity.Wallet;
import com.furkan.digitalWallet.enums.OppositePartyType;
import com.furkan.digitalWallet.enums.TransactionStatus;
import com.furkan.digitalWallet.enums.TransactionType;
import com.furkan.digitalWallet.exception.BadRequestException;
import com.furkan.digitalWallet.exception.NotFoundException;
import com.furkan.digitalWallet.repository.TransactionRepository;
import com.furkan.digitalWallet.request.DepositRequest;
import com.furkan.digitalWallet.request.TransactionDecisionRequest;
import com.furkan.digitalWallet.request.WithdrawRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final WalletService walletService;

    @Transactional(
            isolation = Isolation.REPEATABLE_READ,
            propagation = Propagation.REQUIRED,
            rollbackFor = Exception.class
    )
    public Transaction decide(Long transactionId, TransactionDecisionRequest req) {
        Transaction t = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new NotFoundException("İşlem bulunamadı"));

        if (t.getStatus() != TransactionStatus.PENDING) {
            throw new BadRequestException("Sadece bekleyen işlemler onay/ret edilebilir");
        }

        walletService.processTransactionDecision(t, req.getStatus());

        if (req.getStatus() == TransactionStatus.APPROVED) {
            t.setStatus(TransactionStatus.APPROVED);
        } else if (req.getStatus() == TransactionStatus.DENIED) {
            t.setStatus(TransactionStatus.DENIED);
        } else {
            throw new BadRequestException("Geçersiz durum");
        }

        t.setUpdatedAt(LocalDateTime.now());
        return transactionRepository.save(t);
    }

    @Transactional(
            isolation = Isolation.REPEATABLE_READ,
            propagation = Propagation.REQUIRED,
            rollbackFor = Exception.class
    )
    public Transaction deposit(DepositRequest req, Customer actingUser) {
        Wallet wallet = walletService.processDeposit(req, actingUser);

        Transaction t = createTransaction(wallet, req.getAmount(), TransactionType.DEPOSIT,
                req.getOppositePartyType(), req.getSource());

        boolean isPending = req.getAmount().compareTo(BigDecimal.valueOf(1000)) > 0;
        t.setStatus(isPending ? TransactionStatus.PENDING : TransactionStatus.APPROVED);

        return transactionRepository.save(t);
    }

    @Transactional(
            isolation = Isolation.REPEATABLE_READ,
            propagation = Propagation.REQUIRED,
            rollbackFor = Exception.class
    )
    public Transaction withdraw(WithdrawRequest req, Customer actingUser) {
        Wallet wallet = walletService.processWithdraw(req, actingUser);

        Transaction t = createTransaction(wallet, req.getAmount(), TransactionType.WITHDRAW,
                req.getOppositePartyType(), req.getDestination());

        boolean isPending = req.getAmount().compareTo(BigDecimal.valueOf(1000)) > 0;
        t.setStatus(isPending ? TransactionStatus.PENDING : TransactionStatus.APPROVED);

        return transactionRepository.save(t);
    }

    @Transactional(readOnly = true)
    public List<Transaction> listTransactions(Long walletId, Customer actingUser) {
        Wallet wallet = walletService.getWalletForAccess(walletId, actingUser);
        return transactionRepository.findByWalletOrderByCreatedAtDesc(wallet);
    }

    @Transactional(readOnly = true)
    public Transaction findById(Long transactionId) {
        return transactionRepository.findById(transactionId)
                .orElseThrow(() -> new NotFoundException("İşlem bulunamadı"));
    }

    private Transaction createTransaction(Wallet wallet, BigDecimal amount, TransactionType type,
                                          OppositePartyType oppositePartyType, String oppositeParty) {
        Transaction t = new Transaction();
        t.setWallet(wallet);
        t.setAmount(amount);
        t.setType(type);
        t.setOppositePartyType(oppositePartyType);
        t.setOppositeParty(oppositeParty);
        t.setCreatedAt(LocalDateTime.now());
        return t;
    }
}
