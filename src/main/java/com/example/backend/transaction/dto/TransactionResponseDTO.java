package com.example.backend.transaction.dto;

import com.example.backend.transaction.entity.embeddable.PersonalFinanceCategory;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class TransactionResponseDTO {
    private String transactionId;
    private String accountId;
    private Double amount;
    private String isoCurrencyCode;
    private LocalDate date;
    private String name;
    private String merchantName;
    private String originalDescription;
    private Boolean pending;
    private LocalDate authorizedDate;
    private String paymentChannel;
    private PersonalFinanceCategory personalFinanceCategory;
}
