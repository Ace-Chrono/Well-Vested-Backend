package com.example.backend.transaction.dto;

import com.example.backend.transaction.entity.embeddable.PersonalFinanceCategory;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TransactionUpdateRequestDTO {
    private Double amount;
    private String name;
    private String merchantName;
    private String paymentChannel;
    private PersonalFinanceCategory personalFinanceCategory;
}
