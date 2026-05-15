package com.example.backend.plaid.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlaidInvestmentHoldingDto {
    private String accountId;
    private String securityId;

    private Double quantity;
    private Double institutionPrice;
    private Double institutionValue;
}
