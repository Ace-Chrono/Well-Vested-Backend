package com.example.backend.transaction.service;

import com.example.backend.transaction.dto.TransactionCreateRequestDTO;
import com.example.backend.transaction.dto.TransactionFilterRequestDTO;
import com.example.backend.transaction.dto.TransactionResponseDTO;
import com.example.backend.transaction.dto.TransactionUpdateRequestDTO;

import java.util.List;

public interface TransactionService {
    /*
    This is essentially where you define all your CRUD methods, and your business/application logic. Plus, calls to Plaid,
    DTO conversions, and other stuff.
     */

    TransactionResponseDTO saveTransaction(TransactionCreateRequestDTO dto);
    TransactionResponseDTO getTransactionById(String transactionId);
    List<TransactionResponseDTO> getTransactions(TransactionFilterRequestDTO filter);
    TransactionResponseDTO updateTransaction(
            TransactionUpdateRequestDTO dto,
            String transactionId
    );
    void deleteTransaction(String transactionId);
    void syncTransactions(String accessToken);
}
