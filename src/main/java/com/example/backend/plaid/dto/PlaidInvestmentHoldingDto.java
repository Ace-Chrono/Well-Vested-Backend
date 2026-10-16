package com.example.backend.plaid.dto;

import com.example.backend.investment.entity.AssetType;
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
    private String symbol;
    private String name;
    private AssetType assetType;
    private Double shares;
    private Double price;
    private String isoCurrencyCode;
}
