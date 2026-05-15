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
}
