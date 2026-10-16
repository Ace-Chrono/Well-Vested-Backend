package com.example.backend.insight.service.impls;

import com.example.backend.insight.InsightRepository;
import com.example.backend.insight.dto.InsightResponseDTO;
import com.example.backend.insight.entity.Insight;
import com.example.backend.insight.mapper.InsightMapper;
import com.example.backend.insight.service.InsightService;
import com.example.backend.transaction.TransactionRepository;
import com.example.backend.transaction.entity.Transaction;
import com.example.backend.transaction.service.TransactionService;
import com.example.backend.investment.InvestmentRepository;
import com.example.backend.investment.entity.Investment;
import com.example.backend.investment.entity.AssetType;
import com.example.backend.investment.service.InvestmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
public class InsightServiceImpl implements InsightService {
    private final InsightRepository insightRepository;
    private final InsightMapper insightMapper;
    private final TransactionRepository transactionRepository;
    private final InvestmentRepository investmentRepository;
    private final TransactionService transactionService;
    private final InvestmentService investmentService;

    @Override
    @Transactional(readOnly = true)
    public List<InsightResponseDTO> getSpendingInsights(String currency) {
        return insightMapper.toResponseDTOList(insightRepository
            .findByScopeAndIsoCurrencyCodeOrderByTypeAscSubjectAsc("SPENDING", currency(currency)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<InsightResponseDTO> getInvestmentInsights(String currency) {
        return insightMapper.toResponseDTOList(insightRepository
            .findByScopeAndIsoCurrencyCodeOrderByTypeAscSubjectAsc("INVESTMENTS", currency(currency)));
    }

    @Override
    @Transactional
    public List<InsightResponseDTO> generateSpendingInsights(LocalDate startDate, LocalDate endDate, String currency, boolean sync) {
        String code = currency(currency);
        if (startDate == null || endDate == null || startDate.isAfter(endDate)
                || ChronoUnit.DAYS.between(startDate, endDate) > 365) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Provide an ordered date range of at most 366 days");
        }
        if (sync) transactionService.syncTransactions();
        long days = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        LocalDate previousStart = startDate.minusDays(days);
        LocalDate previousEnd = startDate.minusDays(1);
        List<Transaction> eligible = transactionRepository.findAll().stream()
            .filter(t -> code.equals(t.getIsoCurrencyCode()))
            .filter(t -> t.getDate() != null && t.getAmount() != null && Double.isFinite(t.getAmount()))
            .filter(t -> !Boolean.TRUE.equals(t.getPending()) && t.getAmount() > 0)
            .filter(t -> !Set.of("TRANSFER_IN", "TRANSFER_OUT", "LOAN_PAYMENTS", "INCOME").contains(category(t)))
            .toList();
        List<Transaction> current = eligible.stream().filter(t -> inRange(t, startDate, endDate)).toList();
        List<Transaction> previous = eligible.stream().filter(t -> inRange(t, previousStart, previousEnd)).toList();
        double total = current.stream().mapToDouble(Transaction::getAmount).sum();
        double prior = previous.stream().mapToDouble(Transaction::getAmount).sum();
        List<Insight> results = new ArrayList<>();
        results.add(insight("SPENDING", "SPENDING_TOTAL", "INFO", null,
            "Recorded posted spending for the selected period.", code, total, null, null, startDate, endDate));
        results.add(insight("SPENDING", "SPENDING_CHANGE", "INFO", null,
            prior == 0 ? "No recorded spending baseline in the preceding equal-length period; percentage change is unavailable."
                : "Comparison with recorded spending in the preceding equal-length period.",
            code, total, prior, prior == 0 ? null : (total - prior) / prior * 100, startDate, endDate));
        Map<String, Double> categories = new TreeMap<>();
        current.forEach(t -> categories.merge(category(t), t.getAmount(), Double::sum));
        categories.forEach((name, amount) -> results.add(insight("SPENDING", "CATEGORY_SPENDING", "INFO", name,
            "Recorded spending in this category.", code, amount, null, total == 0 ? null : amount / total * 100, startDate, endDate)));
        if (current.isEmpty()) results.add(insight("SPENDING", "NO_DATA", "INFO", null,
            "No eligible posted spending in this currency and date range. Sync Plaid or check the selected period.",
            code, null, null, null, startDate, endDate));
        return replace("SPENDING", code, results);
    }

    @Override
    @Transactional
    public List<InsightResponseDTO> generateInvestmentInsights(String currency, boolean sync) {
        String code = currency(currency);
        if (sync) investmentService.syncInvestments();
        List<Investment> all = investmentRepository.findAll();
        List<Investment> holdings = all.stream()
            .filter(i -> code.equals(i.getIsoCurrencyCode()))
            .filter(i -> i.getShares() != null && i.getPrice() != null)
            .filter(i -> Double.isFinite(i.getShares()) && Double.isFinite(i.getPrice()))
            .filter(i -> i.getShares() > 0 && i.getPrice() >= 0 && Double.isFinite(i.getShares() * i.getPrice()))
            .toList();
        double total = holdings.stream().mapToDouble(i -> i.getShares() * i.getPrice()).sum();
        List<Insight> results = new ArrayList<>();
        results.add(insight("INVESTMENTS", "PORTFOLIO_VALUE", "INFO", null,
            "Estimated value of eligible long holdings in this currency, using stored institution prices.",
            code, total, null, null, null, null));
        Map<String, Double> positions = new TreeMap<>();
        for (Investment holding : holdings) {
            if (holding.getAssetType() == AssetType.CASH) continue;
            String identity = holding.getSymbol() != null && !holding.getSymbol().isBlank()
                ? "symbol:" + holding.getSymbol().toUpperCase(Locale.ROOT)
                : holding.getSecurityId() != null ? "security:" + holding.getSecurityId() : "record:" + holding.getInvestmentId();
            positions.merge(identity, holding.getShares() * holding.getPrice(), Double::sum);
        }
        if (total > 0) {
            positions.forEach((identity, amount) -> {
                double percent = amount / total * 100;
                if (percent >= 40) results.add(insight("INVESTMENTS", "HOLDING_CONCENTRATION", "WARNING", identity,
                    "This security represents at least 40% of eligible portfolio value. This is a concentration signal, not an overall risk score.",
                    code, amount, null, percent, null, null));
            });
            double crypto = holdings.stream().filter(i -> i.getAssetType() == AssetType.CRYPTO)
                .mapToDouble(i -> i.getShares() * i.getPrice()).sum();
            double percent = crypto / total * 100;
            results.add(insight("INVESTMENTS", "CRYPTO_EXPOSURE", percent >= 20 ? "WARNING" : "INFO", null,
                "Crypto share of eligible portfolio value; the application flags exposure of at least 20%.",
                code, crypto, null, percent, null, null));
        } else results.add(insight("INVESTMENTS", "NO_DATA", "INFO", null,
            "No positive eligible portfolio value in this currency. Sync Plaid or check holdings.", code, null, null, null, null, null));
        if (holdings.size() != all.size()) results.add(insight("INVESTMENTS", "DATA_COVERAGE", "INFO", null,
            "Some records were excluded due to another or missing currency, missing prices, zero quantities, or short positions. Percentages describe eligible holdings only.",
            code, (double) (all.size() - holdings.size()), null, null, null, null));
        return replace("INVESTMENTS", code, results);
    }

    private List<InsightResponseDTO> replace(String scope, String currency, List<Insight> results) {
        insightRepository.deleteByScopeAndIsoCurrencyCode(scope, currency);
        return insightMapper.toResponseDTOList(insightRepository.saveAll(results));
    }

    private boolean inRange(Transaction transaction, LocalDate start, LocalDate end) {
        return !transaction.getDate().isBefore(start) && !transaction.getDate().isAfter(end);
    }

    private String category(Transaction transaction) {
        return transaction.getPersonalFinanceCategory() == null || transaction.getPersonalFinanceCategory().getPrimaryCategory() == null
            ? "UNCATEGORIZED" : transaction.getPersonalFinanceCategory().getPrimaryCategory();
    }

    private String currency(String currency) {
        String code = currency == null ? "" : currency.trim().toUpperCase(Locale.ROOT);
        try { Currency.getInstance(code); }
        catch (IllegalArgumentException e) { throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Provide an ISO currency code"); }
        return code;
    }

    private Insight insight(String scope, String type, String severity, String subject, String message, String currency,
            Double value, Double previousValue, Double percentage, LocalDate start, LocalDate end) {
        Insight entity = new Insight();
        entity.setScope(scope);
        entity.setType(type);
        entity.setSeverity(severity);
        entity.setSubject(subject);
        entity.setMessage(message);
        entity.setIsoCurrencyCode(currency);
        entity.setValue(value);
        entity.setPreviousValue(previousValue);
        entity.setPercentage(percentage);
        entity.setStartDate(start);
        entity.setEndDate(end);
        entity.setGeneratedAt(Instant.now());
        return entity;
    }
}
