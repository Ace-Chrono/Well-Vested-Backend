package com.example.backend.investment.dto;

import com.example.backend.investment.entity.AssetType;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class InvestmentFilterRequestDTO {
    private String symbol;
    private AssetType assetType;
}
