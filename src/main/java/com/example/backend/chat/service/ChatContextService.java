package com.example.backend.chat.service;

import com.example.backend.chat.dto.ChatRequestDTO;
import com.example.backend.insight.InsightRepository;
import com.example.backend.transaction.TransactionRepository;
import com.example.backend.investment.InvestmentRepository;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ChatContextService {
    private final InsightRepository insightRepository;
    private final TransactionRepository transactionRepository;
    private final InvestmentRepository investmentRepository;

    @Transactional(readOnly = true)
    public String buildContext(ChatRequestDTO request) {
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("currency", request.getCurrency());
        context.put("startDate", request.getStartDate().toString());
        context.put("endDate", request.getEndDate().toString());
        context.put("assembledAt", java.time.Instant.now().toString());
        context.put("coverage", "At most 50 latest insights, 100 latest transactions in the date range and 100 holdings. Lists may be truncated; never total samples as a complete portfolio or spending ledger. No live sync performed.");
        context.put("insights", insightRepository.findByIsoCurrencyCodeOrderByGeneratedAtDescInsightIdAsc(request.getCurrency(), PageRequest.of(0, 50))
            .stream().map(i -> row("type", i.getType(), "scope", i.getScope(), "subject", clip(i.getSubject()),
                "message", clip(i.getMessage()), "value", i.getValue(), "previousValue", i.getPreviousValue(),
                "percentage", i.getPercentage(), "severity", i.getSeverity(), "startDate", string(i.getStartDate()),
                "endDate", string(i.getEndDate()), "generatedAt", string(i.getGeneratedAt()))).toList());
        context.put("transactions", transactionRepository.findByIsoCurrencyCodeAndDateBetweenOrderByDateDescTransactionIdAsc(
            request.getCurrency(), request.getStartDate(), request.getEndDate(), PageRequest.of(0, 100))
            .stream().map(t -> row("date", string(t.getDate()), "amount", t.getAmount(), "name", clip(t.getName()),
                "merchant", clip(t.getMerchantName()), "pending", t.getPending(),
                "category", t.getPersonalFinanceCategory() == null ? null : clip(t.getPersonalFinanceCategory().getPrimaryCategory()))).toList());
        context.put("holdings", investmentRepository.findByIsoCurrencyCodeOrderByInvestmentIdAsc(request.getCurrency(), PageRequest.of(0, 100))
            .stream().map(i -> row("symbol", clip(i.getSymbol()), "name", clip(i.getName()), "assetType", string(i.getAssetType()),
                "shares", i.getShares(), "price", i.getPrice())).toList());
        return new Gson().toJson(context);
    }

    private Map<String, Object> row(Object... values) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (int i = 0; i < values.length; i += 2) {
            Object value = values[i + 1];
            if (value instanceof Double number && !Double.isFinite(number)) value = null;
            result.put((String) values[i], value);
        }
        return result;
    }

    private String clip(String text) { return text == null ? null : text.substring(0, Math.min(text.length(), 500)); }
    private String string(Object value) { return value == null ? null : value.toString(); }
}
