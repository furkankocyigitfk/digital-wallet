package com.furkan.digitalWallet.repository;

import com.furkan.digitalWallet.entity.Transaction;
import com.furkan.digitalWallet.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByWalletIdOrderByCreatedAtDesc(Long walletId);
    List<Transaction> findByWalletOrderByCreatedAtDesc(Wallet wallet);
}

