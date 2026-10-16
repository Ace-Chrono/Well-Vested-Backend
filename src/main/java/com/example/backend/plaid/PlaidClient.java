package com.example.backend.plaid;

import com.example.backend.plaid.dto.PlaidInvestmentHoldingDto;
import com.example.backend.investment.entity.AssetType;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import com.example.backend.plaid.dto.PlaidTokenExchangeDto;
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
            .products(List.of(Products.TRANSACTIONS))
            .optionalProducts(List.of(Products.INVESTMENTS));

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

  public PlaidTokenExchangeDto exchangeToken(String publicToken) {

    ItemPublicTokenExchangeRequest request =
        new ItemPublicTokenExchangeRequest()
            .publicToken(publicToken);

    try {
      Response<ItemPublicTokenExchangeResponse> response =
          plaidApi.itemPublicTokenExchange(request).execute();

      if (!response.isSuccessful() || response.body() == null) {
        throw new RuntimeException(
            "Failed to exchange Plaid public token"
        );
      }

      ItemPublicTokenExchangeResponse body = response.body();

      return new PlaidTokenExchangeDto(
          body.getAccessToken(),
          body.getItemId()
      );

    } catch (Exception e) {
      throw new RuntimeException(
          "Plaid token exchange error",
          e
      );
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
    PlaidTransactionDto dto = new PlaidTransactionDto(
        txn.getTransactionId(),
        txn.getAccountId(),
        txn.getName(),
        txn.getAmount(),
        txn.getDate(),
        txn.getCategory() != null && !txn.getCategory().isEmpty()
            ? txn.getCategory().get(0)
            : null,
        txn.getMerchantName(),
        Boolean.TRUE.equals(txn.getPending())
    );
    dto.setIsoCurrencyCode(txn.getIsoCurrencyCode());
    if (txn.getPersonalFinanceCategory() != null) {
      var category = txn.getPersonalFinanceCategory();
      dto.setPersonalFinanceCategory(new com.example.backend.transaction.entity.embeddable.PersonalFinanceCategory(
          category.getPrimary(), category.getDetailed(), category.getConfidenceLevel()));
    }
    return dto;
  }

  public List<PlaidInvestmentHoldingDto> getInvestmentHoldings(String accessToken) {
    InvestmentsHoldingsGetRequest request = new InvestmentsHoldingsGetRequest()
        .accessToken(accessToken);

    try {
      Response<InvestmentsHoldingsGetResponse> response =
          plaidApi.investmentsHoldingsGet(request).execute();

      if (!response.isSuccessful() || response.body() == null) {
        throw new RuntimeException("Failed to fetch Plaid investment holdings");
      }

      InvestmentsHoldingsGetResponse body = response.body();
      if (body.getHoldings() == null || body.getSecurities() == null) {
        throw new IllegalStateException("Incomplete Plaid holdings response");
      }
      Map<String, Security> securities = body.getSecurities().stream()
          .collect(Collectors.toMap(Security::getSecurityId, Function.identity()));

      return body.getHoldings().stream().map(holding -> {
        Security security = securities.get(holding.getSecurityId());
        if (security == null || holding.getAccountId() == null || holding.getSecurityId() == null) {
          throw new IllegalStateException("Plaid holding is missing account or security information");
        }
        return new PlaidInvestmentHoldingDto(
            holding.getAccountId(), holding.getSecurityId(), security.getTickerSymbol(),
            security.getName(), toAssetType(security.getType()),
            holding.getQuantity(), holding.getInstitutionPrice(), holding.getIsoCurrencyCode());
      }).toList();
    } catch (Exception e) {
      throw new RuntimeException("Error fetching investment holdings", e);
    }
  }

  private AssetType toAssetType(String type) {
    if (type == null) return AssetType.OTHER;
    return switch (type) {
      case "equity" -> AssetType.STOCK;
      case "cryptocurrency" -> AssetType.CRYPTO;
      case "etf" -> AssetType.ETF;
      case "mutual fund" -> AssetType.MUTUAL_FUND;
      case "bond" -> AssetType.BOND;
      case "cash" -> AssetType.CASH;
      case "option" -> AssetType.OPTION;
      default -> AssetType.OTHER;
    };
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

  public String createSandboxPublicToken() {

    SandboxPublicTokenCreateRequest request =
        new SandboxPublicTokenCreateRequest()
            .institutionId("ins_109508")
            .initialProducts(List.of(Products.TRANSACTIONS, Products.INVESTMENTS))
            .options(
                new SandboxPublicTokenCreateRequestOptions()
                    .overrideUsername("user_good")
                    .overridePassword("pass_good")
            );

    try {
      Response<SandboxPublicTokenCreateResponse> response =
          plaidApi.sandboxPublicTokenCreate(request).execute();

      if (!response.isSuccessful() || response.body() == null) {
        throw new RuntimeException(
            "Failed to create sandbox public token"
        );
      }

      return response.body().getPublicToken();

    } catch (Exception e) {
      throw new RuntimeException(
          "Plaid sandbox public token error",
          e
      );
    }
  }
}