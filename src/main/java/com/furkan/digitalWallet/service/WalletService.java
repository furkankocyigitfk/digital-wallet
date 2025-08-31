package com.furkan.digitalWallet.service;

import com.furkan.digitalWallet.entity.Customer;
import com.furkan.digitalWallet.entity.Transaction;
import com.furkan.digitalWallet.entity.Wallet;
import com.furkan.digitalWallet.enums.Currency;
import com.furkan.digitalWallet.enums.OppositePartyType;
import com.furkan.digitalWallet.enums.TransactionStatus;
import com.furkan.digitalWallet.enums.TransactionType;
import com.furkan.digitalWallet.exception.BadRequestException;
import com.furkan.digitalWallet.exception.NotFoundException;
import com.furkan.digitalWallet.repository.CustomerRepository;
import com.furkan.digitalWallet.repository.WalletRepository;
import com.furkan.digitalWallet.request.DepositRequest;
import com.furkan.digitalWallet.request.WalletCreateRequest;
import com.furkan.digitalWallet.request.WithdrawRequest;
import com.furkan.digitalWallet.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WalletService {
    private final WalletRepository walletRepository;
    private final CustomerRepository customerRepository;

    @Transactional(
            isolation = Isolation.READ_COMMITTED,
            propagation = Propagation.REQUIRED,
            rollbackFor = Exception.class
    )
    public Wallet createWallet(WalletCreateRequest req, Customer actingUser) {
        Long customerId = (SecurityUtil.hasRole("EMPLOYEE") && req.getCustomerId() != null)
                ? req.getCustomerId() : actingUser.getId();

        if (!customerRepository.existsById(customerId)) {
            throw new NotFoundException("Müşteri bulunamadı: " + customerId);
        }
        if (walletRepository.existsByCustomerIdAndWalletNameIgnoreCase(customerId, req.getWalletName())) {
            throw new BadRequestException("Aynı isimde cüzdan mevcut");
        }
        Wallet w = new Wallet();
        Customer ownerRef = customerRepository.getReferenceById(customerId);
        w.setCustomer(ownerRef);
        w.setWalletName(req.getWalletName());
        w.setCurrency(req.getCurrency());
        w.setActiveForShopping(req.getActiveForShopping() != null ? req.getActiveForShopping() : Boolean.TRUE);
        w.setActiveForWithdraw(req.getActiveForWithdraw() != null ? req.getActiveForWithdraw() : Boolean.TRUE);
        return walletRepository.save(w);
    }

    @Transactional(readOnly = true)
    public List<Wallet> listWallets(Long customerId, Currency currency, Customer actingUser) {
        Long cid = SecurityUtil.hasRole("EMPLOYEE") ? (customerId != null ? customerId : actingUser.getId()) : actingUser.getId();
        if (currency != null) return walletRepository.findByCustomerIdAndCurrency(cid, currency);
        return walletRepository.findByCustomerId(cid);
    }

    @Transactional(
            isolation = Isolation.REPEATABLE_READ,
            propagation = Propagation.REQUIRED,
            rollbackFor = Exception.class
    )
    public Wallet updateBalance(Long walletId, BigDecimal balanceChange, BigDecimal usableBalanceChange) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new NotFoundException("Cüzdan bulunamadı"));

        if (balanceChange != null) {
            wallet.setBalance(wallet.getBalance().add(balanceChange));
        }

        if (usableBalanceChange != null) {
            wallet.setUsableBalance(wallet.getUsableBalance().add(usableBalanceChange));
        }

        return walletRepository.save(wallet);
    }

    /**
     * TransactionService tarafından çağrılan transaction decision işlemi
     * REQUIRES_NEW propagation ile ayrı transaction'da çalışır
     */
    @Transactional(
            isolation = Isolation.REPEATABLE_READ,
            propagation = Propagation.REQUIRES_NEW,
            rollbackFor = Exception.class
    )
    public Wallet processTransactionDecision(Transaction transaction, TransactionStatus newStatus) {
        Wallet w = transaction.getWallet();

        if (newStatus == TransactionStatus.APPROVED) {
            if (transaction.getType() == TransactionType.DEPOSIT) {
                // Deposit onay: usable balance'a ekle
                w.setUsableBalance(w.getUsableBalance().add(transaction.getAmount()));
            } else {
                // Withdraw onay: balance'dan düş
                w.setBalance(w.getBalance().subtract(transaction.getAmount()));
            }
        } else if (newStatus == TransactionStatus.DENIED) {
            if (transaction.getType() == TransactionType.DEPOSIT) {
                // Deposit red: balance'dan geri al
                w.setBalance(w.getBalance().subtract(transaction.getAmount()));
            } else {
                // Withdraw red: usable balance'a geri ver
                w.setUsableBalance(w.getUsableBalance().add(transaction.getAmount()));
            }
        }

        return walletRepository.save(w);
    }

    @Transactional(
            isolation = Isolation.REPEATABLE_READ,
            propagation = Propagation.REQUIRED,
            rollbackFor = Exception.class
    )
    public Wallet processDeposit(DepositRequest req, Customer actingUser) {
        Wallet wallet = getWalletForAccess(req.getWalletId(), actingUser);

        boolean isPending = req.getAmount().compareTo(BigDecimal.valueOf(1000)) > 0;
        if (isPending) {
            // Pending deposit: sadece balance'a ekle
            wallet.setBalance(wallet.getBalance().add(req.getAmount()));
        } else {
            // Approved deposit: hem balance hem usableBalance'a ekle
            wallet.setBalance(wallet.getBalance().add(req.getAmount()));
            wallet.setUsableBalance(wallet.getUsableBalance().add(req.getAmount()));
        }

        return walletRepository.save(wallet);
    }

    @Transactional(
            isolation = Isolation.REPEATABLE_READ,
            propagation = Propagation.REQUIRED,
            rollbackFor = Exception.class
    )
    public Wallet processWithdraw(WithdrawRequest req, Customer actingUser) {
        Wallet wallet = getWalletForAccess(req.getWalletId(), actingUser);

        validateWalletForWithdraw(wallet, req);

        if (wallet.getUsableBalance().compareTo(req.getAmount()) < 0) {
            throw new BadRequestException("Yetersiz kullanılabilir bakiye");
        }

        boolean isPending = req.getAmount().compareTo(BigDecimal.valueOf(1000)) > 0;
        if (isPending) {
            // Pending withdraw: sadece usableBalance'dan düş
            wallet.setUsableBalance(wallet.getUsableBalance().subtract(req.getAmount()));
        } else {
            // Approved withdraw: hem balance hem usableBalance'dan düş
            wallet.setUsableBalance(wallet.getUsableBalance().subtract(req.getAmount()));
            wallet.setBalance(wallet.getBalance().subtract(req.getAmount()));
        }

        return walletRepository.save(wallet);
    }

    @Transactional(readOnly = true)
    public Wallet getWalletForAccess(Long walletId, Customer actingUser) {
        Wallet w = walletRepository.findById(walletId).orElseThrow(() -> new NotFoundException("Cüzdan bulunamadı"));
        if (!SecurityUtil.hasRole("EMPLOYEE") && !w.getCustomer().getId().equals(actingUser.getId())) {
            throw new BadRequestException("Bu cüzdana erişim yetkiniz yok");
        }
        return w;
    }

    @Transactional(readOnly = true)
    public Wallet findById(Long walletId) {
        return walletRepository.findById(walletId)
                .orElseThrow(() -> new NotFoundException("Cüzdan bulunamadı"));
    }

    private void validateWalletForWithdraw(Wallet wallet, WithdrawRequest req) {
        if (req.getOppositePartyType() == OppositePartyType.PAYMENT && !Boolean.TRUE.equals(wallet.getActiveForShopping())) {
            throw new BadRequestException("Cüzdan alışverişe kapalı");
        }
        if (req.getOppositePartyType() == OppositePartyType.IBAN && !Boolean.TRUE.equals(wallet.getActiveForWithdraw())) {
            throw new BadRequestException("Cüzdan çekime kapalı");
        }
    }
}
