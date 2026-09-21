package com.example.backend.plaid;

import com.example.backend.plaid.dto.PlaidTransactionDto;
import com.example.backend.plaid.dto.PlaidTransactionSyncDto;
import com.plaid.client.model.*;
import com.plaid.client.request.PlaidApi;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import retrofit2.Response;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PlaidClient {

  private final PlaidApi plaidApi;

  public String createLinkToken(String userId) {

    LinkTokenCreateRequest request =
        new LinkTokenCreateRequest()
            .clientName("My App")
            .language("en")
            .countryCodes(List.of(CountryCode.US))
            .user(new LinkTokenCreateRequestUser()
                .clientUserId(userId))
            .products(List.of(Products.TRANSACTIONS));

    try {
      Response<LinkTokenCreateResponse> response =
          plaidApi.linkTokenCreate(request).execute();

      if (!response.isSuccessful() || response.body() == null) {
        throw new RuntimeException("Failed to create Plaid link token");
      }

      return response.body().getLinkToken();

    } catch (Exception e) {
      throw new RuntimeException("Plaid link token error", e);
    }
  }

  public String exchangeToken(String publicToken) {

    ItemPublicTokenExchangeRequest request =
        new ItemPublicTokenExchangeRequest()
            .publicToken(publicToken);

    try {
      Response<ItemPublicTokenExchangeResponse> response =
          plaidApi.itemPublicTokenExchange(request).execute();

      if (!response.isSuccessful() || response.body() == null) {
        throw new RuntimeException("Failed to exchange Plaid public token");
      }

      return response.body().getAccessToken();

    } catch (Exception e) {
      throw new RuntimeException("Plaid token exchange error", e);
    }
  }

  public PlaidTransactionSyncDto syncTransactions(
      String accessToken,
      String cursor
  ) {

    TransactionsSyncRequest request =
        new TransactionsSyncRequest()
            .accessToken(accessToken);

    // First sync has no cursor.
    if (cursor != null && !cursor.isBlank()) {
      request.setCursor(cursor);
    }

    try {
      Response<TransactionsSyncResponse> response =
          plaidApi.transactionsSync(request).execute();

      if (!response.isSuccessful() || response.body() == null) {
        String errorBody = response.errorBody() != null
            ? response.errorBody().string()
            : "unknown error";

        throw new RuntimeException("Plaid error: " + errorBody);
      }

      TransactionsSyncResponse body = response.body();

      List<PlaidTransactionDto> added = body.getAdded()
          .stream()
          .map(this::toTransactionDto)
          .toList();

      List<PlaidTransactionDto> modified = body.getModified()
          .stream()
          .map(this::toTransactionDto)
          .toList();

      List<String> removedTransactionIds = body.getRemoved()
          .stream()
          .map(RemovedTransaction::getTransactionId)
          .toList();

      return new PlaidTransactionSyncDto(
          added,
          modified,
          removedTransactionIds,
          body.getNextCursor(),
          body.getHasMore()
      );

    } catch (Exception e) {
      throw new RuntimeException("Error syncing transactions", e);
    }
  }

  private PlaidTransactionDto toTransactionDto(Transaction txn) {
    return new PlaidTransactionDto(
        txn.getTransactionId(),
        txn.getAccountId(),
        txn.getName(),
        txn.getAmount(),
        txn.getDate(),
        txn.getCategory() != null && !txn.getCategory().isEmpty()
            ? txn.getCategory().get(0)
            : null,
        txn.getMerchantName(),
        txn.getPending()
    );
  }

  public void removeItem(String accessToken) {

    ItemRemoveRequest request =
        new ItemRemoveRequest()
            .accessToken(accessToken);

    try {
      plaidApi.itemRemove(request).execute();

    } catch (Exception e) {
      throw new RuntimeException("Error removing item", e);
    }
  }
}