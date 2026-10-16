package com.example.backend.insight;

import com.example.backend.insight.dto.InsightResponseDTO;
import com.example.backend.insight.mapper.InsightMapper;
import com.example.backend.insight.service.impls.InsightServiceImpl;
import com.example.backend.transaction.TransactionRepository;
import com.example.backend.transaction.entity.Transaction;
import com.example.backend.transaction.entity.embeddable.PersonalFinanceCategory;
import com.example.backend.transaction.service.TransactionService;
import com.example.backend.investment.InvestmentRepository;
import com.example.backend.investment.entity.Investment;
import com.example.backend.investment.entity.AssetType;
import com.example.backend.investment.service.InvestmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

class InsightServiceImplTest {
    private final InsightRepository insights = mock(InsightRepository.class);
    private final TransactionRepository transactions = mock(TransactionRepository.class);
    private final InvestmentRepository investments = mock(InvestmentRepository.class);
    private final TransactionService transactionService = mock(TransactionService.class);
    private final InvestmentService investmentService = mock(InvestmentService.class);
    private final InsightServiceImpl service = new InsightServiceImpl(insights, Mappers.getMapper(InsightMapper.class),
        transactions, investments, transactionService, investmentService);
    private final LocalDate start = LocalDate.of(2026, 9, 1);
    private final LocalDate end = LocalDate.of(2026, 9, 30);

    @BeforeEach
    void setup() {
        lenient().when(insights.saveAll(anyList())).thenAnswer(call -> call.getArgument(0));
    }

    @Test
    void spendingExcludesPendingInflowsTransfersAndOtherCurrencies() {
        Transaction pending = transaction(500, start, "USD", "FOOD_AND_DRINK");
        pending.setPending(true);
        when(transactions.findAll()).thenReturn(List.of(
            transaction(150, start, "USD", "FOOD_AND_DRINK"),
            transaction(100, start.minusDays(1), "USD", "FOOD_AND_DRINK"),
            transaction(-200, start, "USD", "INCOME"),
            transaction(500, start, "USD", "TRANSFER_OUT"),
            transaction(500, start, "EUR", "FOOD_AND_DRINK"), pending));
        List<InsightResponseDTO> results = service.generateSpendingInsights(start, end, "usd", false);
        InsightResponseDTO change = find(results, "SPENDING_CHANGE");
        assertEquals(150.0, change.getValue());
        assertEquals(100.0, change.getPreviousValue());
        assertEquals(50.0, change.getPercentage());
        assertEquals(100.0, find(results, "CATEGORY_SPENDING").getPercentage());
        verify(insights).deleteByScopeAndIsoCurrencyCode("SPENDING", "USD");
        verifyNoInteractions(transactionService);
    }

    @Test
    void emptyHistoryHasNoInventedPercentage() {
        when(transactions.findAll()).thenReturn(List.of());
        List<InsightResponseDTO> result = service.generateSpendingInsights(start, end, "USD", true);
        assertNull(find(result, "SPENDING_CHANGE").getPercentage());
        assertNotNull(find(result, "NO_DATA"));
        verify(transactionService).syncTransactions();
    }

    @Test
    void aggregatesSameSecurityAcrossAccountsAndFlagsCrypto() {
        Investment first = holding("AAPL", AssetType.STOCK, 30, "USD");
        Investment second = holding("AAPL", AssetType.STOCK, 20, "USD");
        Investment crypto = holding("BTC", AssetType.CRYPTO, 25, "USD");
        Investment cash = holding(null, AssetType.CASH, 25, "USD");
        when(investments.findAll()).thenReturn(List.of(first, second, crypto, cash, holding("OTHER", AssetType.STOCK, 900, "EUR")));
        List<InsightResponseDTO> result = service.generateInvestmentInsights("USD", true);
        assertEquals(100.0, find(result, "PORTFOLIO_VALUE").getValue());
        assertEquals(50.0, find(result, "HOLDING_CONCENTRATION").getPercentage());
        assertEquals("symbol:AAPL", find(result, "HOLDING_CONCENTRATION").getSubject());
        assertEquals("WARNING", find(result, "CRYPTO_EXPOSURE").getSeverity());
        assertEquals(25.0, find(result, "CRYPTO_EXPOSURE").getPercentage());
        assertEquals(1.0, find(result, "DATA_COVERAGE").getValue());
        verify(investmentService).syncInvestments();
    }

    @Test
    void validatesBeforeCallingPlaidOrWriting() {
        assertThrows(ResponseStatusException.class, () -> service.generateSpendingInsights(end, start, "USD", true));
        assertThrows(ResponseStatusException.class, () -> service.generateInvestmentInsights("INVALID", true));
        verifyNoInteractions(transactionService, investmentService, insights);
    }

    @Test
    void failedSyncDoesNotReplacePreviousInsights() {
        doThrow(new RuntimeException("Plaid unavailable")).when(investmentService).syncInvestments();
        assertThrows(RuntimeException.class, () -> service.generateInvestmentInsights("USD", true));
        verifyNoInteractions(insights, investments);
    }

    @Test
    void transactionMapperRetainsPlaidCurrencyAndFinanceCategory() {
        com.example.backend.plaid.dto.PlaidTransactionDto dto = new com.example.backend.plaid.dto.PlaidTransactionDto();
        dto.setTransactionId("plaid-id");
        dto.setIsoCurrencyCode("USD");
        dto.setPersonalFinanceCategory(new PersonalFinanceCategory("FOOD_AND_DRINK", "RESTAURANTS", "HIGH"));
        Transaction mapped = Mappers.getMapper(com.example.backend.transaction.mapper.TransactionMapper.class).toEntity(dto);
        assertEquals("USD", mapped.getIsoCurrencyCode());
        assertEquals("FOOD_AND_DRINK", mapped.getPersonalFinanceCategory().getPrimaryCategory());
        assertEquals("plaid-id", mapped.getPlaidTransactionId());
    }

    private Transaction transaction(double amount, LocalDate date, String currency, String category) {
        Transaction t = new Transaction();
        t.setAmount(amount);
        t.setDate(date);
        t.setIsoCurrencyCode(currency);
        t.setPersonalFinanceCategory(new PersonalFinanceCategory(category, null, null));
        return t;
    }

    private Investment holding(String symbol, AssetType type, double value, String currency) {
        return new Investment(UUID.randomUUID(), symbol, symbol, type, 1.0, value, null, null, null, null, currency);
    }

    private InsightResponseDTO find(List<InsightResponseDTO> results, String type) {
        return results.stream().filter(i -> type.equals(i.getType())).findFirst().orElseThrow();
    }
}
