package com.example.backend.plaid.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlaidTransactionDto {
    private String transactionId;
    private String accountId;
    private String name;
    private Double amount;
    private LocalDate date;

    private String category;
    private String merchantName;
    private boolean pending;
    private String isoCurrencyCode;
    private com.example.backend.transaction.entity.embeddable.PersonalFinanceCategory personalFinanceCategory;

    public PlaidTransactionDto(String transactionId, String accountId, String name, Double amount,
            LocalDate date, String category, String merchantName, boolean pending) {
        this.transactionId = transactionId;
        this.accountId = accountId;
        this.name = name;
        this.amount = amount;
        this.date = date;
        this.category = category;
        this.merchantName = merchantName;
        this.pending = pending;
    }
}
