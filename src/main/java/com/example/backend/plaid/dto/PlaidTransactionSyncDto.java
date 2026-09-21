package com.example.backend.plaid.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class PlaidTransactionSyncDto {
  private List<PlaidTransactionDto> added;
  private List<PlaidTransactionDto> modified;
  private List<String> removedTransactionIds;

  private String nextCursor;
  private boolean hasMore;
}