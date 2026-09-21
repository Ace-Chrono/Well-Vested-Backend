package com.example.backend.plaid.service.impl;

import com.example.backend.plaid.PlaidClient;
import com.example.backend.plaid.dto.PlaidTransactionSyncDto;
import com.example.backend.plaid.service.PlaidService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PlaidServiceImpl implements PlaidService {

  private final PlaidClient plaidClient;

  @Override
  public String createLinkToken(String userId) {
    return plaidClient.createLinkToken(userId);
  }

  @Override
  public String exchangePublicToken(String publicToken) {
    return plaidClient.exchangeToken(publicToken);
  }

  @Override
  public PlaidTransactionSyncDto syncTransactions(
      String accessToken,
      String cursor
  ) {
    return plaidClient.syncTransactions(accessToken, cursor);
  }

  @Override
  public void removeItem(String accessToken) {
    plaidClient.removeItem(accessToken);
  }
}