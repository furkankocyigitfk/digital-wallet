package com.furkan.digitalWallet.request;

import com.furkan.digitalWallet.enums.OppositePartyType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DepositRequest {
    @NotNull
    private Long walletId;

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal amount;

    @NotNull
    private OppositePartyType oppositePartyType; // IBAN or PAYMENT

    @NotBlank
    private String source; // iban or payment id
}

