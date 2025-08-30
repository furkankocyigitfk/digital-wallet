package com.furkan.digitalWallet.request;

import com.furkan.digitalWallet.enums.TransactionStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TransactionDecisionRequest {
    @NotNull
    private TransactionStatus status;
}

