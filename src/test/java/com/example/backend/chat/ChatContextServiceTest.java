package com.example.backend.chat;

import com.example.backend.chat.dto.ChatRequestDTO;
import com.example.backend.chat.service.ChatContextService;
import com.example.backend.insight.InsightRepository;
import com.example.backend.transaction.TransactionRepository;
import com.example.backend.transaction.entity.Transaction;
import com.example.backend.investment.InvestmentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

class ChatContextServiceTest {
    @Test
    void usesBoundedCurrencyQueriesAndOmitsAccountIdentifiers() {
        var insights = mock(InsightRepository.class);
        var transactions = mock(TransactionRepository.class);
        var holdings = mock(InvestmentRepository.class);
        Transaction t = new Transaction(); t.setAccountId("private-account"); t.setPlaidTransactionId("private-id");
        t.setName("Merchant"); t.setAmount(Double.NaN);
        when(insights.findByIsoCurrencyCodeOrderByGeneratedAtDescInsightIdAsc(eq("USD"), any())).thenReturn(List.of());
        when(holdings.findByIsoCurrencyCodeOrderByInvestmentIdAsc(eq("USD"), any())).thenReturn(List.of());
        when(transactions.findByIsoCurrencyCodeAndDateBetweenOrderByDateDescTransactionIdAsc(eq("USD"), any(), any(), any())).thenReturn(List.of(t));
        String context = new ChatContextService(insights, transactions, holdings).buildContext(new ChatRequestDTO());
        assertTrue(context.contains("Merchant")); assertFalse(context.contains("private-account"));
        assertFalse(context.contains("private-id")); assertFalse(context.contains("NaN"));
        var page = org.mockito.ArgumentCaptor.forClass(Pageable.class);
        verify(transactions).findByIsoCurrencyCodeAndDateBetweenOrderByDateDescTransactionIdAsc(eq("USD"), any(), any(), page.capture());
        assertEquals(100, page.getValue().getPageSize());
    }
}
