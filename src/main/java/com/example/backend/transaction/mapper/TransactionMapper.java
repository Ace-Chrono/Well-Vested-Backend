package com.example.backend.transaction.mapper;

import com.example.backend.plaid.dto.PlaidTransactionDto;
import com.example.backend.transaction.dto.TransactionCreateRequestDTO;
import com.example.backend.transaction.dto.TransactionResponseDTO;
import com.example.backend.transaction.dto.TransactionUpdateRequestDTO;
import com.example.backend.transaction.entity.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TransactionMapper {
    Transaction toEntity(TransactionCreateRequestDTO dto);
    TransactionResponseDTO toResponseDTO(Transaction entity);
    List<TransactionResponseDTO> toResponseDTOList(List<Transaction> transactions);
    void updateTransactionFromDto(TransactionUpdateRequestDTO dto, @MappingTarget Transaction entity);
    Transaction toEntity(PlaidTransactionDto dto);
}
