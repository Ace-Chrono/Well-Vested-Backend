package com.example.backend.insight.controller;

import com.example.backend.insight.dto.InsightResponseDTO;
import com.example.backend.insight.service.InsightService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/insights")
@RequiredArgsConstructor
public class InsightController {
    private final InsightService insightService;

    @GetMapping("/spending")
    public List<InsightResponseDTO> getSpendingInsights(@RequestParam(defaultValue = "USD") String currency) {
        return insightService.getSpendingInsights(currency);
    }

    @GetMapping("/investments")
    public List<InsightResponseDTO> getInvestmentInsights(@RequestParam(defaultValue = "USD") String currency) {
        return insightService.getInvestmentInsights(currency);
    }

    @PostMapping("/spending/generate")
    public List<InsightResponseDTO> generateSpendingInsights(
            @RequestParam LocalDate startDate, @RequestParam LocalDate endDate,
            @RequestParam(defaultValue = "USD") String currency) {
        return insightService.generateSpendingInsights(startDate, endDate, currency, false);
    }

    @PostMapping("/spending/sync")
    public List<InsightResponseDTO> syncSpendingInsights(
            @RequestParam LocalDate startDate, @RequestParam LocalDate endDate,
            @RequestParam(defaultValue = "USD") String currency) {
        return insightService.generateSpendingInsights(startDate, endDate, currency, true);
    }

    @PostMapping("/investments/generate")
    public List<InsightResponseDTO> generateInvestmentInsights(@RequestParam(defaultValue = "USD") String currency) {
        return insightService.generateInvestmentInsights(currency, false);
    }

    @PostMapping("/investments/sync")
    public List<InsightResponseDTO> syncInvestmentInsights(@RequestParam(defaultValue = "USD") String currency) {
        return insightService.generateInvestmentInsights(currency, true);
    }
}
