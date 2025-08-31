package com.furkan.digitalWallet.service;

import com.furkan.digitalWallet.request.TransactionDecisionRequest;
import com.furkan.digitalWallet.entity.Transaction;
import com.furkan.digitalWallet.entity.Wallet;
import com.furkan.digitalWallet.enums.TransactionStatus;
import com.furkan.digitalWallet.enums.TransactionType;
import com.furkan.digitalWallet.exception.BadRequestException;
import com.furkan.digitalWallet.exception.NotFoundException;
import com.furkan.digitalWallet.repository.TransactionRepository;
import com.furkan.digitalWallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;

    @Transactional
    public Transaction decide(Long transactionId, TransactionDecisionRequest req) {
        Transaction t = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new NotFoundException("İşlem bulunamadı"));
        if (t.getStatus() != TransactionStatus.PENDING) {
            throw new BadRequestException("Sadece bekleyen işlemler onay/ret edilebilir");
        }
        Wallet w = t.getWallet();
        if (req.getStatus() == TransactionStatus.APPROVED) {
            if (t.getType() == TransactionType.DEPOSIT) {

                w.setUsableBalance(w.getUsableBalance().add(t.getAmount()));
            } else {

                w.setBalance(w.getBalance().subtract(t.getAmount()));
            }
            t.setStatus(TransactionStatus.APPROVED);
        } else if (req.getStatus() == TransactionStatus.DENIED) {
            if (t.getType() == TransactionType.DEPOSIT) {

                w.setBalance(w.getBalance().subtract(t.getAmount()));
            } else {

                w.setUsableBalance(w.getUsableBalance().add(t.getAmount()));
            }
            t.setStatus(TransactionStatus.DENIED);
        } else {
            throw new BadRequestException("Geçersiz durum");
        }
        t.setUpdatedAt(LocalDateTime.now());
        walletRepository.save(w);
        return transactionRepository.save(t);
    }
}

