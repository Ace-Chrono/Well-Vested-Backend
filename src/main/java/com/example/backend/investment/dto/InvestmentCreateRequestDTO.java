package com.example.backend.investment.dto;

import com.example.backend.investment.entity.AssetType;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.*;

@Getter
@Setter
@NoArgsConstructor
public class InvestmentCreateRequestDTO {
    @NotBlank
    @Pattern(regexp = "[A-Za-z0-9][A-Za-z0-9.\\-]{0,19}")
    private String symbol;
    @NotBlank
    private String name;
    @NotNull
    private AssetType assetType;
    @NotNull
    @DecimalMin("0.0")
    private Double shares;
    @NotNull
    @DecimalMin("0.0")
    private Double price;
    @NotNull
    private Double dayChange;
}
