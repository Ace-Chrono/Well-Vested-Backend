package com.example.backend.insight.service;

import com.example.backend.insight.dto.InsightResponseDTO;
import java.time.LocalDate;
import java.util.List;

public interface InsightService {
    List<InsightResponseDTO> getSpendingInsights(String currency);
    List<InsightResponseDTO> getInvestmentInsights(String currency);
    List<InsightResponseDTO> generateSpendingInsights(LocalDate startDate, LocalDate endDate, String currency, boolean sync);
    List<InsightResponseDTO> generateInvestmentInsights(String currency, boolean sync);
}
