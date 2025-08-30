package com.furkan.digitalWallet.repository;

import com.furkan.digitalWallet.entity.Wallet;
import com.furkan.digitalWallet.enums.Currency;

import java.util.List;
import java.util.Optional;

public interface WalletRepository extends org.springframework.data.jpa.repository.JpaRepository<Wallet, Long> {
    List<Wallet> findByCustomerId(Long customerId);
    List<Wallet> findByCustomerIdAndCurrency(Long customerId, Currency currency);
    Optional<Wallet> findByIdAndCustomerId(Long id, Long customerId);
    boolean existsByCustomerIdAndWalletNameIgnoreCase(Long customerId, String walletName);
}
