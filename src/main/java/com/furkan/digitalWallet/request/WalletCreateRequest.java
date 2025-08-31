package com.furkan.digitalWallet.request;

import com.furkan.digitalWallet.enums.Currency;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class WalletCreateRequest {

    private Long customerId;

    @NotBlank
    private String walletName;

    @NotNull
    private Currency currency;

    private Boolean activeForShopping = true;
    private Boolean activeForWithdraw = true;
}

