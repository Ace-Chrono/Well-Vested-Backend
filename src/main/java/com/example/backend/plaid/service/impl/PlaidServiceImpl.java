package com.example.backend.plaid.service.impl;

import com.example.backend.plaid.PlaidClient;
import com.example.backend.plaid.dto.PlaidTokenExchangeDto;
import com.example.backend.plaid.dto.PlaidTransactionSyncDto;
import com.example.backend.plaid.entity.PlaidItem;
import com.example.backend.plaid.repository.PlaidItemRepository;
import com.example.backend.plaid.service.PlaidService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PlaidServiceImpl implements PlaidService {

  private final PlaidClient plaidClient;
  private final PlaidItemRepository plaidItemRepository;

  @Override
  public String createLinkToken(String userId) {
    return plaidClient.createLinkToken(userId);
  }

    @Override
  public void exchangePublicToken(String publicToken) {

    PlaidTokenExchangeDto exchange =
        plaidClient.exchangeToken(publicToken);

    PlaidItem item = new PlaidItem();

    item.setPlaidItemId(exchange.getItemId());
    item.setAccessToken(exchange.getAccessToken());

    // No transaction sync has happened yet.
    item.setTransactionCursor(null);

    plaidItemRepository.save(item);
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