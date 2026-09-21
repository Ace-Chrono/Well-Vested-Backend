package com.example.backend.transaction.controller;

import com.example.backend.transaction.dto.TransactionCreateRequestDTO;
import com.example.backend.transaction.dto.TransactionFilterRequestDTO;
import com.example.backend.transaction.dto.TransactionResponseDTO;
import com.example.backend.transaction.dto.TransactionUpdateRequestDTO;
import com.example.backend.transaction.service.TransactionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionResponseDTO saveTransaction(
            @Valid @RequestBody TransactionCreateRequestDTO dto
    ) {
        return transactionService.saveTransaction(dto);
    }

    @GetMapping("/{transactionId}")
    public TransactionResponseDTO getTransactionById(
            @PathVariable UUID transactionId
    ) {
        return transactionService.getTransactionById(transactionId);
    }

    @PostMapping("/filter")
    public List<TransactionResponseDTO> getTransactions(
            @RequestBody TransactionFilterRequestDTO filter
    ) {
        return transactionService.getTransactions(filter);
    }

    @PatchMapping("/{transactionId}")
    public TransactionResponseDTO updateTransaction(
            @PathVariable UUID transactionId,
            @RequestBody TransactionUpdateRequestDTO dto
    ) {
        return transactionService.updateTransaction(dto, transactionId);
    }

    @DeleteMapping("/{transactionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTransaction(
            @PathVariable UUID transactionId
    ) {
        transactionService.deleteTransaction(transactionId);
    }

    @PostMapping("/sync")
    @ResponseStatus(HttpStatus.OK)
    public void syncTransactions() {
        transactionService.syncTransactions();
    }
}