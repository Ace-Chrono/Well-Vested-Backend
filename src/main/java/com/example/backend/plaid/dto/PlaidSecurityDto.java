package com.example.backend.plaid.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlaidSecurityDto {
    private String securityId;
    private String name;
    private String tickerSymbol;
    private String type;
    private String isoCurrencyCode;
}
