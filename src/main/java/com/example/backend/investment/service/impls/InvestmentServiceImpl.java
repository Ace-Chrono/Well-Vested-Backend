package com.example.backend.investment.service.impls;

import com.example.backend.investment.InvestmentRepository;
import com.example.backend.investment.dto.InvestmentCreateRequestDTO;
import com.example.backend.investment.dto.InvestmentFilterRequestDTO;
import com.example.backend.investment.dto.InvestmentResponseDTO;
import com.example.backend.investment.dto.InvestmentUpdateRequestDTO;
import com.example.backend.investment.entity.Investment;
import com.example.backend.investment.mapper.InvestmentMapper;
import com.example.backend.investment.service.InvestmentService;
import com.example.backend.plaid.PlaidClient;
import com.example.backend.plaid.dto.PlaidInvestmentHoldingDto;
import com.example.backend.plaid.entity.PlaidItem;
import com.example.backend.plaid.repository.PlaidItemRepository;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InvestmentServiceImpl implements InvestmentService {
    private final InvestmentRepository investmentRepository;
    private final InvestmentMapper investmentMapper;
    private final PlaidClient plaidClient;
    private final PlaidItemRepository plaidItemRepository;

    @Override
    public InvestmentResponseDTO saveInvestment(InvestmentCreateRequestDTO dto) {
        Investment entity = investmentMapper.toEntity(dto);
        Investment saved = investmentRepository.save(entity);

        return investmentMapper.toResponseDTO(saved);
    }

    @Override
    public InvestmentResponseDTO getInvestmentById(UUID investmentId) {
        Investment entity = investmentRepository.findById(investmentId)
            .orElseThrow(() -> new EntityNotFoundException("Investment not found"));

        return investmentMapper.toResponseDTO(entity);
    }

    @Override
    public List<InvestmentResponseDTO> getInvestments(InvestmentFilterRequestDTO filter) {
        return investmentRepository.findAll().stream()
            .filter(i -> filter.getSymbol() == null || filter.getSymbol().equalsIgnoreCase(i.getSymbol()))
            .filter(i -> filter.getAssetType() == null || filter.getAssetType() == i.getAssetType())
            .map(investmentMapper::toResponseDTO).toList();
    }

    @Override
    public InvestmentResponseDTO updateInvestment(InvestmentUpdateRequestDTO dto, UUID investmentId) {
        Investment entity = investmentRepository.findById(investmentId)
            .orElseThrow(() -> new EntityNotFoundException("Investment not found"));
        investmentMapper.updateInvestmentFromDto(dto, entity);
        Investment updated = investmentRepository.save(entity);

        return investmentMapper.toResponseDTO(updated);
    }

    @Override
    public void deleteInvestment(UUID investmentId) {
        if (!investmentRepository.existsById(investmentId)) {
            throw new EntityNotFoundException("Investment not found");
        }

        investmentRepository.deleteById(investmentId);
    }
    @Override
    @Transactional
    public void syncInvestments() {
        PlaidItem item = plaidItemRepository.findFirstBy()
            .orElseThrow(() -> new EntityNotFoundException("No Plaid item connected"));

        // Holdings is a full snapshot, unlike the transaction cursor API.
        List<PlaidInvestmentHoldingDto> holdings = plaidClient.getInvestmentHoldings(item.getAccessToken());
        Map<List<String>, Investment> existing = new HashMap<>();
        for (Investment investment : investmentRepository.findByPlaidItemId(item.getPlaidItemId())) {
            existing.put(List.of(investment.getAccountId(), investment.getSecurityId()), investment);
        }
        for (PlaidInvestmentHoldingDto holding : holdings) {
            Investment entity = investmentMapper.toEntity(holding);
            Investment previous = existing.remove(List.of(holding.getAccountId(), holding.getSecurityId()));
            if (previous != null) entity.setInvestmentId(previous.getInvestmentId());
            entity.setPlaidItemId(item.getPlaidItemId());
            investmentRepository.save(entity);
        }
        // Remove positions no longer held, preserving manual records and other Items.
        investmentRepository.deleteAll(existing.values());
    }

}
