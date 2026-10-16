package com.example.backend.investment.dto;

import com.example.backend.investment.entity.AssetType;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.*;

@Getter
@Setter
@NoArgsConstructor
public class InvestmentUpdateRequestDTO {
    @Pattern(regexp = "[A-Za-z0-9][A-Za-z0-9.\\-]{0,19}")
    private String symbol;
    private String name;
    private AssetType assetType;
    @DecimalMin("0.0")
    private Double shares;
    @DecimalMin("0.0")
    private Double price;
    private Double dayChange;
}
