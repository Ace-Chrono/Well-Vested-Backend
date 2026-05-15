package com.example.backend.plaid;

import com.example.backend.plaid.dto.PlaidAccountDto;
import com.example.backend.plaid.dto.PlaidInvestmentHoldingDto;
import com.example.backend.plaid.dto.PlaidSecurityDto;
import com.example.backend.plaid.dto.PlaidTransactionDto;
import com.plaid.client.request.PlaidApi;
import com.plaid.client.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import retrofit2.Response;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

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
            Response<LinkTokenCreateResponse> response = plaidApi
                    .linkTokenCreate(request)
                    .execute();

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

            return response.body().getAccessToken();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List fetchAccounts(String accessToken) {
        AccountsGetRequest request =
                new AccountsGetRequest()
                        .accessToken(accessToken);

        try {
            Response<AccountsGetResponse> response =
                    plaidApi.accountsGet(request).execute();

            return response.body()
                    .getAccounts()
                    .stream()
                    .map(acc -> new PlaidAccountDto(
                            acc.getAccountId(),
                            acc.getName(),
                            acc.getOfficialName(),
                            acc.getSubtype() != null ? acc.getSubtype().toString() : null,
                            acc.getMask(),
                            acc.getBalances().getAvailable(),
                            acc.getBalances().getCurrent(),
                            acc.getBalances().getIsoCurrencyCode()
                    ))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            throw new RuntimeException("Error fetching accounts", e);
        }
    }

    public List fetchTransactions(String accessToken, String start, String end) {
        TransactionsGetRequest request =
                new TransactionsGetRequest()
                        .accessToken(accessToken)
                        .startDate(LocalDate.parse(start))
                        .endDate(LocalDate.parse(end));

        try {
            Response<TransactionsGetResponse> response =
                    plaidApi.transactionsGet(request).execute();

            return response.body()
                    .getTransactions()
                    .stream()
                    .map(txn -> new PlaidTransactionDto(
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
                    ))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            throw new RuntimeException("Error fetching transactions", e);
        }
    }

    public List fetchHoldings(String accessToken) {
        InvestmentsHoldingsGetRequest request =
                new InvestmentsHoldingsGetRequest()
                        .accessToken(accessToken);

        try {
            Response<InvestmentsHoldingsGetResponse> response =
                    plaidApi.investmentsHoldingsGet(request).execute();

            return response.body()
                    .getHoldings()
                    .stream()
                    .map(h -> new PlaidInvestmentHoldingDto(
                            h.getAccountId(),
                            h.getSecurityId(),
                            h.getQuantity(),
                            h.getInstitutionPrice(),
                            h.getInstitutionValue()
                    ))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            throw new RuntimeException("Error fetching holdings", e);
        }
    }

    public List fetchSecurities(String accessToken) {
        InvestmentsHoldingsGetRequest request =
                new InvestmentsHoldingsGetRequest()
                        .accessToken(accessToken);

        try {
            Response<InvestmentsHoldingsGetResponse> response =
                    plaidApi.investmentsHoldingsGet(request).execute();

            return response.body()
                    .getSecurities()
                    .stream()
                    .map(sec -> new PlaidSecurityDto(
                            sec.getSecurityId(),
                            sec.getName(),
                            sec.getTickerSymbol(),
                            sec.getType() != null ? sec.getType() : null,
                            sec.getIsoCurrencyCode()
                    ))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            throw new RuntimeException("Error fetching securities", e);
        }
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
