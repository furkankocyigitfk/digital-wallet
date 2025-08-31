package com.furkan.digitalWallet.controller;

import com.furkan.digitalWallet.entity.Customer;
import com.furkan.digitalWallet.entity.Transaction;
import com.furkan.digitalWallet.request.DepositRequest;
import com.furkan.digitalWallet.request.TransactionDecisionRequest;
import com.furkan.digitalWallet.request.WithdrawRequest;
import com.furkan.digitalWallet.security.SecurityUtil;
import com.furkan.digitalWallet.service.CustomerService;
import com.furkan.digitalWallet.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;
    private final CustomerService customerService;

    @PostMapping("/deposit")
    public ResponseEntity<Transaction> deposit(@Valid @RequestBody DepositRequest req) {
        Customer acting = customerService.getByUsername(SecurityUtil.currentUsername());
        Transaction t = transactionService.deposit(req, acting);
        return ResponseEntity.ok(t);
    }

    @PostMapping("/withdraw")
    public ResponseEntity<Transaction> withdraw(@Valid @RequestBody WithdrawRequest req) {
        Customer acting = customerService.getByUsername(SecurityUtil.currentUsername());
        Transaction t = transactionService.withdraw(req, acting);
        return ResponseEntity.ok(t);
    }

    @PreAuthorize("hasRole('EMPLOYEE')")
    @PostMapping("/{transactionId}/decision")
    public ResponseEntity<Transaction> decide(@PathVariable Long transactionId,
                                              @Valid @RequestBody TransactionDecisionRequest req) {
        Transaction t = transactionService.decide(transactionId, req);
        return ResponseEntity.ok(t);
    }
}
