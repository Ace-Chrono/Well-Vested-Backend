package com.example.backend.investment.controller;

import com.example.backend.investment.dto.InvestmentCreateRequestDTO;
import com.example.backend.investment.dto.InvestmentFilterRequestDTO;
import com.example.backend.investment.dto.InvestmentResponseDTO;
import com.example.backend.investment.dto.InvestmentUpdateRequestDTO;
import com.example.backend.investment.service.InvestmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/investments")
@RequiredArgsConstructor
public class InvestmentController {
    private final InvestmentService investmentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InvestmentResponseDTO saveInvestment(@Valid @RequestBody InvestmentCreateRequestDTO dto) {
        return investmentService.saveInvestment(dto);
    }

    @GetMapping
    public List<InvestmentResponseDTO> getInvestments(@ModelAttribute InvestmentFilterRequestDTO filter) {
        return investmentService.getInvestments(filter);
    }

    @GetMapping("/{investmentId}")
    public InvestmentResponseDTO getInvestmentById(@PathVariable UUID investmentId) {
        return investmentService.getInvestmentById(investmentId);
    }

    @PostMapping("/filter")
    public List<InvestmentResponseDTO> filterInvestments(@RequestBody InvestmentFilterRequestDTO filter) {
        return investmentService.getInvestments(filter);
    }

    @PatchMapping("/{investmentId}")
    public InvestmentResponseDTO updateInvestment(@PathVariable UUID investmentId,
            @RequestBody InvestmentUpdateRequestDTO dto) {
        return investmentService.updateInvestment(dto, investmentId);
    }

    @DeleteMapping("/{investmentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteInvestment(@PathVariable UUID investmentId) {
        investmentService.deleteInvestment(investmentId);
    }
    @PostMapping("/sync")
    @ResponseStatus(HttpStatus.OK)
    public void syncInvestments() {
        investmentService.syncInvestments();
    }

}
