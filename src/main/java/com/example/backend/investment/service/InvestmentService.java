package com.example.backend.investment.service;

import com.example.backend.investment.dto.InvestmentCreateRequestDTO;
import com.example.backend.investment.dto.InvestmentFilterRequestDTO;
import com.example.backend.investment.dto.InvestmentResponseDTO;
import com.example.backend.investment.dto.InvestmentUpdateRequestDTO;
import java.util.List;
import java.util.UUID;

public interface InvestmentService {
    InvestmentResponseDTO saveInvestment(InvestmentCreateRequestDTO dto);
    InvestmentResponseDTO getInvestmentById(UUID investmentId);
    List<InvestmentResponseDTO> getInvestments(InvestmentFilterRequestDTO filter);
    InvestmentResponseDTO updateInvestment(InvestmentUpdateRequestDTO dto, UUID investmentId);
    void syncInvestments();
    void deleteInvestment(UUID investmentId);
}
