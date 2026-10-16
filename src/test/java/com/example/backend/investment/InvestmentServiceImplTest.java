package com.example.backend.investment;

import com.example.backend.investment.dto.*;
import com.example.backend.investment.entity.AssetType;
import com.example.backend.investment.entity.Investment;
import com.example.backend.investment.mapper.InvestmentMapper;
import com.example.backend.investment.service.impls.InvestmentServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class InvestmentServiceImplTest {
    private final InvestmentRepository repository = mock(InvestmentRepository.class);
    private final com.example.backend.plaid.PlaidClient plaidClient = mock(com.example.backend.plaid.PlaidClient.class);
    private final com.example.backend.plaid.repository.PlaidItemRepository itemRepository = mock(com.example.backend.plaid.repository.PlaidItemRepository.class);
    private final InvestmentServiceImpl service = new InvestmentServiceImpl(
        repository, Mappers.getMapper(InvestmentMapper.class),
        plaidClient, itemRepository);

    @Test
    void filtersHoldingsByTypeAndSymbol() {
        Investment stock = new Investment(UUID.randomUUID(), "AAPL", "Apple", AssetType.STOCK, 12.0, 227.5, 1.8, null, null, null, null);
        Investment crypto = new Investment(UUID.randomUUID(), "BTC", "Bitcoin", AssetType.CRYPTO, 0.42, 91250.0, 2.1, null, null, null, null);
        when(repository.findAll()).thenReturn(List.of(stock, crypto));
        InvestmentFilterRequestDTO filter = new InvestmentFilterRequestDTO();
        filter.setAssetType(AssetType.CRYPTO);
        assertEquals(1, service.getInvestments(filter).size());
        filter.setSymbol("btc");
        assertEquals("BTC", service.getInvestments(filter).get(0).getSymbol());
        filter.setAssetType(AssetType.STOCK);
        assertTrue(service.getInvestments(filter).isEmpty());
    }

    @Test
    void mapsCreateAndReturnsDatabaseGeneratedId() {
        InvestmentCreateRequestDTO create = new InvestmentCreateRequestDTO();
        create.setSymbol("TEST");
        create.setName("Test holding");
        create.setAssetType(AssetType.STOCK);
        create.setShares(0.25);
        create.setPrice(100.12);
        create.setDayChange(-1.5);
        UUID id = UUID.randomUUID();
        when(repository.save(any(Investment.class))).thenAnswer(call -> {
            Investment entity = call.getArgument(0);
            assertNull(entity.getInvestmentId());
            entity.setInvestmentId(id);
            return entity;
        });
        InvestmentResponseDTO result = service.saveInvestment(create);
        assertEquals(id, result.getInvestmentId());
        assertEquals(create.getPrice(), result.getPrice());
        assertEquals(create.getShares(), result.getShares());
    }

    @Test
    void updateUsesTransactionMappingSemanticsAndPreservesId() {
        UUID id = UUID.randomUUID();
        Investment existing = new Investment(id, "AAPL", "Apple", AssetType.STOCK, 12.0, 227.5, 1.8, null, null, null, null);
        when(repository.findById(id)).thenReturn(Optional.of(existing));
        when(repository.save(any(Investment.class))).thenAnswer(call -> call.getArgument(0));
        InvestmentUpdateRequestDTO update = new InvestmentUpdateRequestDTO();
        update.setShares(0.0);
        InvestmentResponseDTO result = service.updateInvestment(update, id);
        assertEquals(id, result.getInvestmentId());
        assertEquals(0.0, result.getShares());
        assertNull(result.getPrice());
        verify(repository).save(existing);
    }

    @Test
    void missingRecordsUseSameExceptionAsTransactions() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> service.getInvestmentById(id));
        assertThrows(EntityNotFoundException.class, () -> service.updateInvestment(new InvestmentUpdateRequestDTO(), id));
        assertThrows(EntityNotFoundException.class, () -> service.deleteInvestment(id));
        verify(repository, never()).deleteById(any());
    }

    @Test
    void deletesExistingInvestment() {
        UUID id = UUID.randomUUID();
        when(repository.existsById(id)).thenReturn(true);
        service.deleteInvestment(id);
        verify(repository).deleteById(id);
    }
    @Test
    void syncUpsertsByAccountAndSecurityAndRemovesMissingPositions() {
        com.example.backend.plaid.entity.PlaidItem item = new com.example.backend.plaid.entity.PlaidItem();
        item.setPlaidItemId("item");
        item.setAccessToken("access");
        when(itemRepository.findFirstBy()).thenReturn(Optional.of(item));
        Investment kept = new Investment(UUID.randomUUID(), "AAPL", "Apple", AssetType.STOCK,
            1.0, 10.0, 1.0, "item", "account1", "security", "USD");
        Investment removed = new Investment(UUID.randomUUID(), "OLD", "Old", AssetType.STOCK,
            1.0, 10.0, 1.0, "item", "account1", "old-security", "USD");
        when(repository.findByPlaidItemId("item")).thenReturn(List.of(kept, removed));
        when(plaidClient.getInvestmentHoldings("access")).thenReturn(List.of(
            new com.example.backend.plaid.dto.PlaidInvestmentHoldingDto("account1", "security", "AAPL", "Apple", AssetType.STOCK, 2.0, 20.0, "USD"),
            new com.example.backend.plaid.dto.PlaidInvestmentHoldingDto("account2", "security", "AAPL", "Apple", AssetType.STOCK, 3.0, 20.0, "USD")));
        service.syncInvestments();
        org.mockito.ArgumentCaptor<Investment> captor = org.mockito.ArgumentCaptor.forClass(Investment.class);
        verify(repository, times(2)).save(captor.capture());
        assertEquals(kept.getInvestmentId(), captor.getAllValues().get(0).getInvestmentId());
        assertEquals(2.0, captor.getAllValues().get(0).getShares());
        assertNull(captor.getAllValues().get(0).getDayChange());
        assertNull(captor.getAllValues().get(1).getInvestmentId());
        assertEquals("item", captor.getAllValues().get(1).getPlaidItemId());
        org.mockito.ArgumentCaptor<Iterable<Investment>> deleted = org.mockito.ArgumentCaptor.forClass(Iterable.class);
        verify(repository).deleteAll(deleted.capture());
        assertEquals(List.of(removed), java.util.stream.StreamSupport.stream(deleted.getValue().spliterator(), false).toList());
    }

    @Test
    void syncFailureLeavesStoredHoldingsUntouched() {
        com.example.backend.plaid.entity.PlaidItem item = new com.example.backend.plaid.entity.PlaidItem();
        item.setAccessToken("access");
        when(itemRepository.findFirstBy()).thenReturn(Optional.of(item));
        when(plaidClient.getInvestmentHoldings("access")).thenThrow(new RuntimeException("Plaid unavailable"));
        assertThrows(RuntimeException.class, service::syncInvestments);
        verifyNoInteractions(repository);
    }

    @Test
    void syncRequiresConnectedItem() {
        when(itemRepository.findFirstBy()).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, service::syncInvestments);
        verifyNoInteractions(plaidClient, repository);
    }

}
