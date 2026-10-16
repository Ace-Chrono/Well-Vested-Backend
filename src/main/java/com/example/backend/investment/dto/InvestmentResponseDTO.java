package com.example.backend.investment.dto;

import com.example.backend.investment.entity.AssetType;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class InvestmentResponseDTO {
    private UUID investmentId;
    private String symbol;
    private String name;
    private AssetType assetType;
    private Double shares;
    private Double price;
    private Double dayChange;
    private String accountId;
    private String securityId;
    private String isoCurrencyCode;
}
