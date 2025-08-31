package com.furkan.digitalWallet.controller;

import com.furkan.digitalWallet.entity.Customer;
import com.furkan.digitalWallet.entity.Transaction;
import com.furkan.digitalWallet.entity.Wallet;
import com.furkan.digitalWallet.enums.Currency;
import com.furkan.digitalWallet.request.WalletCreateRequest;
import com.furkan.digitalWallet.security.SecurityUtil;
import com.furkan.digitalWallet.service.CustomerService;
import com.furkan.digitalWallet.service.TransactionService;
import com.furkan.digitalWallet.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/wallets")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;
    private final CustomerService customerService;
    private final TransactionService transactionService;

    @PostMapping
    public ResponseEntity<Wallet> create(@Valid @RequestBody WalletCreateRequest req) {
        Customer acting = customerService.getByUsername(SecurityUtil.currentUsername());
        Wallet w = walletService.createWallet(req, acting);
        return ResponseEntity.ok(w);
    }

    @GetMapping
    public ResponseEntity<List<Wallet>> list(@RequestParam(required = false) Long customerId,
                                             @RequestParam(required = false) Currency currency) {
        Customer acting = customerService.getByUsername(SecurityUtil.currentUsername());
        List<Wallet> wallets = walletService.listWallets(customerId, currency, acting);
        return ResponseEntity.ok(wallets);
    }

    @GetMapping("/{walletId}/transactions")
    public ResponseEntity<List<Transaction>> listTransactions(@PathVariable Long walletId) {
        Customer acting = customerService.getByUsername(SecurityUtil.currentUsername());
        List<Transaction> txs = transactionService.listTransactions(walletId, acting);
        return ResponseEntity.ok(txs);
    }
}
