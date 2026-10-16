package com.example.backend.investment.mapper;

import com.example.backend.investment.dto.InvestmentCreateRequestDTO;
import com.example.backend.investment.dto.InvestmentResponseDTO;
import com.example.backend.investment.dto.InvestmentUpdateRequestDTO;
import com.example.backend.investment.entity.Investment;
import com.example.backend.plaid.dto.PlaidInvestmentHoldingDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface InvestmentMapper {
    @Mapping(target = "investmentId", ignore = true)
    @Mapping(target = "plaidItemId", ignore = true)
    @Mapping(target = "accountId", ignore = true)
    @Mapping(target = "securityId", ignore = true)
    @Mapping(target = "isoCurrencyCode", ignore = true)
    Investment toEntity(InvestmentCreateRequestDTO dto);
    @Mapping(target = "investmentId", ignore = true)
    @Mapping(target = "plaidItemId", ignore = true)
    @Mapping(target = "dayChange", ignore = true)
    Investment toEntity(PlaidInvestmentHoldingDto dto);
    InvestmentResponseDTO toResponseDTO(Investment entity);
    List<InvestmentResponseDTO> toResponseDTOList(List<Investment> investments);
    @Mapping(target = "investmentId", ignore = true)
    @Mapping(target = "plaidItemId", ignore = true)
    @Mapping(target = "accountId", ignore = true)
    @Mapping(target = "securityId", ignore = true)
    @Mapping(target = "isoCurrencyCode", ignore = true)
    void updateInvestmentFromDto(InvestmentUpdateRequestDTO dto, @MappingTarget Investment entity);
}
