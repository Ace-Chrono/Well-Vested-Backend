package com.example.backend.plaid.service;

import com.example.backend.plaid.dto.PlaidTransactionSyncDto;

public interface PlaidService {

  String createLinkToken(String userId);

  String exchangePublicToken(String publicToken);

  PlaidTransactionSyncDto syncTransactions(String accessToken, String cursor);

  void removeItem(String accessToken);
}