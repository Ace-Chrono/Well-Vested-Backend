package com.example.backend.transaction.service.impls;

import com.example.backend.plaid.PlaidClient;
import com.example.backend.plaid.dto.PlaidTransactionDto;
import com.example.backend.plaid.dto.PlaidTransactionSyncDto;
import com.example.backend.plaid.entity.PlaidItem;
import com.example.backend.plaid.repository.PlaidItemRepository;
import com.example.backend.transaction.TransactionRepository;
import com.example.backend.transaction.dto.TransactionCreateRequestDTO;
import com.example.backend.transaction.dto.TransactionFilterRequestDTO;
import com.example.backend.transaction.dto.TransactionResponseDTO;
import com.example.backend.transaction.dto.TransactionUpdateRequestDTO;
import com.example.backend.transaction.entity.Transaction;
import com.example.backend.transaction.mapper.TransactionMapper;
import com.example.backend.transaction.service.TransactionService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {
    //Uses dependency injection here through the constructor. Don't use @Autowired, outdated.
    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;
    private final PlaidClient plaidClient;
    private final PlaidItemRepository plaidItemRepository;

    @Override
    public TransactionResponseDTO saveTransaction(TransactionCreateRequestDTO dto) {

        Transaction entity = transactionMapper.toEntity(dto);

        Transaction saved = transactionRepository.save(entity);

        return transactionMapper.toResponseDTO(saved);
    }

    @Override
    public TransactionResponseDTO getTransactionById(UUID transactionId) {

        Transaction entity = transactionRepository.findById(transactionId).orElseThrow(() -> new EntityNotFoundException("Transaction not found"));

        return transactionMapper.toResponseDTO(entity);
    }

    @Override
    public List<TransactionResponseDTO> getTransactions(TransactionFilterRequestDTO filter) {

        return transactionRepository.findAll().stream().filter(t -> filter.getStartDate() == null || !t.getDate().isBefore(filter.getStartDate())).filter(t -> filter.getEndDate() == null || !t.getDate().isAfter(filter.getEndDate())).filter(t -> filter.getMerchantName() == null || (t.getMerchantName() != null && t.getMerchantName().toLowerCase().contains(filter.getMerchantName().toLowerCase()))).filter(t -> filter.getPaymentChannel() == null || filter.getPaymentChannel().equalsIgnoreCase(t.getPaymentChannel())).filter(t -> filter.getMinAmount() == null || t.getAmount() >= filter.getMinAmount()).filter(t -> filter.getMaxAmount() == null || t.getAmount() <= filter.getMaxAmount()).filter(t -> filter.getPersonalFinanceCategoryPrimary() == null || (t.getPersonalFinanceCategory() != null && filter.getPersonalFinanceCategoryPrimary().equalsIgnoreCase(t.getPersonalFinanceCategory().getPrimaryCategory()))).map(transactionMapper::toResponseDTO).toList();
    }

    @Override
    public TransactionResponseDTO updateTransaction(TransactionUpdateRequestDTO dto, UUID transactionId) {

        Transaction entity = transactionRepository.findById(transactionId).orElseThrow(() -> new EntityNotFoundException("Transaction not found"));

        transactionMapper.updateTransactionFromDto(dto, entity);

        Transaction updated = transactionRepository.save(entity);

        return transactionMapper.toResponseDTO(updated);
    }

    @Override
    public void deleteTransaction(UUID transactionId) {

        if (!transactionRepository.existsById(transactionId)) {
            throw new EntityNotFoundException("Transaction not found");
        }

        transactionRepository.deleteById(transactionId);
    }

  @Override
  public void syncTransactions() {

    PlaidItem item = plaidItemRepository.findFirstBy()
        .orElseThrow(() ->
            new EntityNotFoundException("No Plaid item connected")
        );

    String cursor = item.getTransactionCursor();

    boolean hasMore;

    do {
      PlaidTransactionSyncDto sync =
          plaidClient.syncTransactions(
              item.getAccessToken(),
              cursor
          );

      // Added
      for (PlaidTransactionDto plaid : sync.getAdded()) {

        Transaction entity =
            transactionMapper.toEntity(plaid);

        entity.setPlaidTransactionId(
            plaid.getTransactionId()
        );

        transactionRepository.save(entity);
      }

      // Modified
      for (PlaidTransactionDto plaid : sync.getModified()) {

        transactionRepository
            .findByPlaidTransactionId(plaid.getTransactionId())
            .ifPresent(existing -> {

              Transaction entity =
                  transactionMapper.toEntity(plaid);

              // Keep our internal UUID
              entity.setTransactionId(
                  existing.getTransactionId()
              );

              entity.setPlaidTransactionId(
                  plaid.getTransactionId()
              );

              transactionRepository.save(entity);
            });
      }

      // Removed
      for (String plaidTransactionId :
          sync.getRemovedTransactionIds()) {

        transactionRepository
            .findByPlaidTransactionId(plaidTransactionId)
            .ifPresent(transactionRepository::delete);
      }

      cursor = sync.getNextCursor();
      hasMore = sync.isHasMore();

    } while (hasMore);

    // Only advance our stored position after all pages succeeded.
    item.setTransactionCursor(cursor);
    plaidItemRepository.save(item);
  }
}
