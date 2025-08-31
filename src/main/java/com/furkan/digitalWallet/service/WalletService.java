package com.furkan.digitalWallet.service;

import com.furkan.digitalWallet.request.DepositRequest;
import com.furkan.digitalWallet.request.WalletCreateRequest;
import com.furkan.digitalWallet.request.WithdrawRequest;
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
import com.furkan.digitalWallet.repository.TransactionRepository;
import com.furkan.digitalWallet.repository.WalletRepository;
import com.furkan.digitalWallet.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WalletService {
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final CustomerRepository customerRepository;

    @Transactional
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

    public List<Wallet> listWallets(Long customerId, Currency currency, Customer actingUser) {
        Long cid = SecurityUtil.hasRole("EMPLOYEE") ? (customerId != null ? customerId : actingUser.getId()) : actingUser.getId();
        if (currency != null) return walletRepository.findByCustomerIdAndCurrency(cid, currency);
        return walletRepository.findByCustomerId(cid);
    }

    @Transactional
    public Transaction deposit(DepositRequest req, Customer actingUser) {
        Wallet wallet = getWalletForAccess(req.getWalletId(), actingUser);
        Transaction t = new Transaction();
        t.setWallet(wallet);
        t.setAmount(req.getAmount());
        t.setType(TransactionType.DEPOSIT);
        t.setOppositePartyType(req.getOppositePartyType());
        t.setOppositeParty(req.getSource());
        t.setCreatedAt(LocalDateTime.now());

        boolean isPending = req.getAmount().compareTo(BigDecimal.valueOf(1000)) > 0;
        if (isPending) {
            t.setStatus(TransactionStatus.PENDING);
            wallet.setBalance(wallet.getBalance().add(req.getAmount()));
        } else {
            t.setStatus(TransactionStatus.APPROVED);
            wallet.setBalance(wallet.getBalance().add(req.getAmount()));
            wallet.setUsableBalance(wallet.getUsableBalance().add(req.getAmount()));
        }
        walletRepository.save(wallet);
        return transactionRepository.save(t);
    }

    @Transactional
    public Transaction withdraw(WithdrawRequest req, Customer actingUser) {
        Wallet wallet = getWalletForAccess(req.getWalletId(), actingUser);

        if (req.getOppositePartyType() == OppositePartyType.PAYMENT && !Boolean.TRUE.equals(wallet.getActiveForShopping())) {
            throw new BadRequestException("Cüzdan alışverişe kapalı");
        }
        if (req.getOppositePartyType() == OppositePartyType.IBAN && !Boolean.TRUE.equals(wallet.getActiveForWithdraw())) {
            throw new BadRequestException("Cüzdan çekime kapalı");
        }


        if (wallet.getUsableBalance().compareTo(req.getAmount()) < 0) {
            throw new BadRequestException("Yetersiz kullanılabilir bakiye");
        }

        Transaction t = new Transaction();
        t.setWallet(wallet);
        t.setAmount(req.getAmount());
        t.setType(TransactionType.WITHDRAW);
        t.setOppositePartyType(req.getOppositePartyType());
        t.setOppositeParty(req.getDestination());
        t.setCreatedAt(LocalDateTime.now());

        boolean isPending = req.getAmount().compareTo(BigDecimal.valueOf(1000)) > 0;
        if (isPending) {
            t.setStatus(TransactionStatus.PENDING);

            wallet.setUsableBalance(wallet.getUsableBalance().subtract(req.getAmount()));
        } else {
            t.setStatus(TransactionStatus.APPROVED);
            wallet.setUsableBalance(wallet.getUsableBalance().subtract(req.getAmount()));
            wallet.setBalance(wallet.getBalance().subtract(req.getAmount()));
        }
        walletRepository.save(wallet);
        return transactionRepository.save(t);
    }

    public List<Transaction> listTransactions(Long walletId, Customer actingUser) {
        Wallet wallet = getWalletForAccess(walletId, actingUser);
        return transactionRepository.findByWalletOrderByCreatedAtDesc(wallet);
    }

    public Wallet getWalletForAccess(Long walletId, Customer actingUser) {
        Wallet w = walletRepository.findById(walletId).orElseThrow(() -> new NotFoundException("Cüzdan bulunamadı"));
        if (!SecurityUtil.hasRole("EMPLOYEE") && !w.getCustomer().getId().equals(actingUser.getId())) {
            throw new BadRequestException("Bu cüzdana erişim yetkiniz yok");
        }
        return w;
    }
}
