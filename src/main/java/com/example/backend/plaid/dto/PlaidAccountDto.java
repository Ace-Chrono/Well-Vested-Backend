package com.example.backend.plaid.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlaidAccountDto {
    private String accountId;
    private String name;
    private String officialName;
    private String subtype;
    private String mask;
    private Double availableBalance;
    private Double currentBalance;
    private String currency;
}
