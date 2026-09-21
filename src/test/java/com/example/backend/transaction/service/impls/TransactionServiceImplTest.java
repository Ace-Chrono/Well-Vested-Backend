package com.example.backend.transaction.service.impls;

import com.example.backend.plaid.PlaidClient;
import com.example.backend.plaid.dto.PlaidTransactionDto;
import com.example.backend.plaid.dto.PlaidTransactionSyncDto;
import com.example.backend.transaction.TransactionRepository;
import com.example.backend.transaction.dto.TransactionCreateRequestDTO;
import com.example.backend.transaction.dto.TransactionFilterRequestDTO;
import com.example.backend.transaction.dto.TransactionResponseDTO;
import com.example.backend.transaction.dto.TransactionUpdateRequestDTO;
import com.example.backend.transaction.entity.Transaction;
import com.example.backend.transaction.mapper.TransactionMapper;
import com.example.backend.plaid.entity.PlaidItem;
import com.example.backend.plaid.repository.PlaidItemRepository;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceImplTest {

  @Mock
  private PlaidItemRepository plaidItemRepository;

  @Mock
  private TransactionRepository transactionRepository;

  @Mock
  private TransactionMapper transactionMapper;

  @Mock
  private PlaidClient plaidClient;

  @InjectMocks
  private TransactionServiceImpl transactionService;

  private PlaidItem plaidItem;
  private Transaction transaction;
  private TransactionCreateRequestDTO createDTO;
  private TransactionResponseDTO responseDTO;
  private TransactionUpdateRequestDTO updateDTO;

  private UUID transactionId;
  private String plaidTransactionId;

  @BeforeEach
  void setUp() {

    // Our application's internal identity
    transactionId = UUID.randomUUID();

    // Plaid's external identity
    plaidTransactionId = "plaid_txn_123";

    transaction = new Transaction();
    transaction.setTransactionId(transactionId);
    transaction.setPlaidTransactionId(plaidTransactionId);
    transaction.setAmount(50.0);

    createDTO = new TransactionCreateRequestDTO();
    createDTO.setAmount(50.0);

    responseDTO = new TransactionResponseDTO();
    responseDTO.setTransactionId(transactionId);
    responseDTO.setAmount(50.0);

    updateDTO = new TransactionUpdateRequestDTO();
    updateDTO.setAmount(100.0);

    plaidItem = new PlaidItem();
    plaidItem.setPlaidItemId("item_123");
    plaidItem.setAccessToken("access-token");
    plaidItem.setTransactionCursor("old-cursor");
  }

  // ---------------------------------------------------------
  // CREATE
  // ---------------------------------------------------------

  @Test
  void saveTransaction_ShouldSaveAndReturnResponseDTO() {

    when(transactionMapper.toEntity(createDTO))
        .thenReturn(transaction);

    when(transactionRepository.save(transaction))
        .thenReturn(transaction);

    when(transactionMapper.toResponseDTO(transaction))
        .thenReturn(responseDTO);

    TransactionResponseDTO result =
        transactionService.saveTransaction(createDTO);

    assertNotNull(result);
    assertEquals(transactionId, result.getTransactionId());
    assertEquals(50.0, result.getAmount());

    verify(transactionMapper, times(1))
        .toEntity(createDTO);

    verify(transactionRepository, times(1))
        .save(transaction);

    verify(transactionMapper, times(1))
        .toResponseDTO(transaction);
  }

  // ---------------------------------------------------------
  // GET BY ID
  // ---------------------------------------------------------

  @Test
  void getTransactionById_shouldReturnTransaction() {

    when(transactionRepository.findById(transactionId))
        .thenReturn(Optional.of(transaction));

    when(transactionMapper.toResponseDTO(transaction))
        .thenReturn(responseDTO);

    TransactionResponseDTO result =
        transactionService.getTransactionById(transactionId);

    assertNotNull(result);
    assertEquals(transactionId, result.getTransactionId());

    verify(transactionRepository, times(1))
        .findById(transactionId);

    verify(transactionMapper, times(1))
        .toResponseDTO(transaction);
  }

  @Test
  void getTransactionById_shouldThrowExceptionWhenNotFound() {

    UUID nonexistentId = UUID.randomUUID();

    when(transactionRepository.findById(nonexistentId))
        .thenReturn(Optional.empty());

    assertThrows(
        EntityNotFoundException.class,
        () -> transactionService.getTransactionById(nonexistentId)
    );

    verify(transactionRepository, times(1))
        .findById(nonexistentId);

    verify(transactionMapper, never())
        .toResponseDTO(any(Transaction.class));
  }

  // ---------------------------------------------------------
  // UPDATE
  // ---------------------------------------------------------

  @Test
  void updateTransaction_shouldUpdateAndReturnResponse() {

    when(transactionRepository.findById(transactionId))
        .thenReturn(Optional.of(transaction));

    when(transactionRepository.save(transaction))
        .thenReturn(transaction);

    when(transactionMapper.toResponseDTO(transaction))
        .thenReturn(responseDTO);

    TransactionResponseDTO result =
        transactionService.updateTransaction(
            updateDTO,
            transactionId
        );

    assertNotNull(result);
    assertEquals(transactionId, result.getTransactionId());

    verify(transactionRepository, times(1))
        .findById(transactionId);

    verify(transactionMapper, times(1))
        .updateTransactionFromDto(updateDTO, transaction);

    verify(transactionRepository, times(1))
        .save(transaction);

    verify(transactionMapper, times(1))
        .toResponseDTO(transaction);
  }

  @Test
  void updateTransaction_shouldThrowExceptionWhenNotFound() {

    UUID nonexistentId = UUID.randomUUID();

    when(transactionRepository.findById(nonexistentId))
        .thenReturn(Optional.empty());

    assertThrows(
        EntityNotFoundException.class,
        () -> transactionService.updateTransaction(
            updateDTO,
            nonexistentId
        )
    );

    verify(transactionRepository, times(1))
        .findById(nonexistentId);

    verify(transactionMapper, never())
        .updateTransactionFromDto(any(), any());

    verify(transactionRepository, never())
        .save(any(Transaction.class));
  }

  // ---------------------------------------------------------
  // DELETE
  // ---------------------------------------------------------

  @Test
  void deleteTransaction_shouldDeleteTransaction() {

    when(transactionRepository.existsById(transactionId))
        .thenReturn(true);

    transactionService.deleteTransaction(transactionId);

    verify(transactionRepository, times(1))
        .existsById(transactionId);

    verify(transactionRepository, times(1))
        .deleteById(transactionId);
  }

  @Test
  void deleteTransaction_shouldThrowExceptionWhenNotFound() {

    UUID nonexistentId = UUID.randomUUID();

    when(transactionRepository.existsById(nonexistentId))
        .thenReturn(false);

    assertThrows(
        EntityNotFoundException.class,
        () -> transactionService.deleteTransaction(nonexistentId)
    );

    verify(transactionRepository, times(1))
        .existsById(nonexistentId);

    verify(transactionRepository, never())
        .deleteById(any(UUID.class));
  }

  // ---------------------------------------------------------
  // FILTER
  // ---------------------------------------------------------

  @Test
  void getTransactions_shouldFilterTransactions() {

    TransactionFilterRequestDTO filter =
        new TransactionFilterRequestDTO();

    filter.setMerchantName("Amazon");

    transaction.setMerchantName("Amazon Marketplace");

    when(transactionRepository.findAll())
        .thenReturn(List.of(transaction));

    when(transactionMapper.toResponseDTO(transaction))
        .thenReturn(responseDTO);

    List<TransactionResponseDTO> results =
        transactionService.getTransactions(filter);

    assertEquals(1, results.size());
    assertEquals(responseDTO, results.get(0));

    verify(transactionRepository, times(1))
        .findAll();

    verify(transactionMapper, times(1))
        .toResponseDTO(transaction);
  }// ---------------------------------------------------------
// PLAID SYNC - ADDED TRANSACTION
// ---------------------------------------------------------

  @Test
  void syncTransactions_shouldInsertAddedTransaction() {

    PlaidTransactionDto plaidDto = new PlaidTransactionDto();
    plaidDto.setTransactionId(plaidTransactionId);

    Transaction newTransaction = new Transaction();
    newTransaction.setAmount(50.0);

    PlaidTransactionSyncDto syncDto =
        new PlaidTransactionSyncDto(
            List.of(plaidDto),
            List.of(),
            List.of(),
            "next-cursor",
            false
        );

    when(plaidItemRepository.findFirstBy())
        .thenReturn(Optional.of(plaidItem));

    when(plaidClient.syncTransactions(
        "access-token",
        "old-cursor"
    )).thenReturn(syncDto);

    when(transactionMapper.toEntity(plaidDto))
        .thenReturn(newTransaction);

    transactionService.syncTransactions();

    assertEquals(
        plaidTransactionId,
        newTransaction.getPlaidTransactionId()
    );

    assertNull(newTransaction.getTransactionId());

    // The completed sync should advance the cursor.
    assertEquals(
        "next-cursor",
        plaidItem.getTransactionCursor()
    );

    verify(transactionRepository)
        .save(newTransaction);

    verify(plaidItemRepository)
        .save(plaidItem);
  }


// ---------------------------------------------------------
// PLAID SYNC - MODIFIED TRANSACTION
// ---------------------------------------------------------

  @Test
  void syncTransactions_shouldUpdateModifiedTransaction() {

    PlaidTransactionDto plaidDto = new PlaidTransactionDto();
    plaidDto.setTransactionId(plaidTransactionId);

    Transaction mappedPlaidTransaction = new Transaction();
    mappedPlaidTransaction.setAmount(75.0);

    PlaidTransactionSyncDto syncDto =
        new PlaidTransactionSyncDto(
            List.of(),
            List.of(plaidDto),
            List.of(),
            "next-cursor",
            false
        );

    when(plaidItemRepository.findFirstBy())
        .thenReturn(Optional.of(plaidItem));

    when(plaidClient.syncTransactions(
        "access-token",
        "old-cursor"
    )).thenReturn(syncDto);

    when(transactionRepository
        .findByPlaidTransactionId(plaidTransactionId))
        .thenReturn(Optional.of(transaction));

    when(transactionMapper.toEntity(plaidDto))
        .thenReturn(mappedPlaidTransaction);

    transactionService.syncTransactions();

    // Preserve our application's UUID.
    assertEquals(
        transactionId,
        mappedPlaidTransaction.getTransactionId()
    );

    // Preserve Plaid's external identity.
    assertEquals(
        plaidTransactionId,
        mappedPlaidTransaction.getPlaidTransactionId()
    );

    assertEquals(
        "next-cursor",
        plaidItem.getTransactionCursor()
    );

    verify(transactionRepository)
        .save(mappedPlaidTransaction);

    verify(plaidItemRepository)
        .save(plaidItem);
  }


// ---------------------------------------------------------
// PLAID SYNC - REMOVED TRANSACTION
// ---------------------------------------------------------

  @Test
  void syncTransactions_shouldDeleteRemovedTransaction() {

    PlaidTransactionSyncDto syncDto =
        new PlaidTransactionSyncDto(
            List.of(),
            List.of(),
            List.of(plaidTransactionId),
            "next-cursor",
            false
        );

    when(plaidItemRepository.findFirstBy())
        .thenReturn(Optional.of(plaidItem));

    when(plaidClient.syncTransactions(
        "access-token",
        "old-cursor"
    )).thenReturn(syncDto);

    when(transactionRepository
        .findByPlaidTransactionId(plaidTransactionId))
        .thenReturn(Optional.of(transaction));

    transactionService.syncTransactions();

    verify(transactionRepository)
        .delete(transaction);

    verify(transactionRepository, never())
        .save(any(Transaction.class));

    assertEquals(
        "next-cursor",
        plaidItem.getTransactionCursor()
    );

    verify(plaidItemRepository)
        .save(plaidItem);
  }
}