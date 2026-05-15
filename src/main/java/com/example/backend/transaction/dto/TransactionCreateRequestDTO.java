package com.example.backend.transaction.dto;

import com.example.backend.transaction.entity.embeddable.PersonalFinanceCategory;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class TransactionCreateRequestDTO { //transaction_id not made yet, will be made by DB.
    private String accountId;
    private Double amount;
    private String isoCurrencyCode;
    private LocalDate date;
    private String name;
    private String merchantName;
    private String paymentChannel;
    private PersonalFinanceCategory personalFinanceCategory;
}
