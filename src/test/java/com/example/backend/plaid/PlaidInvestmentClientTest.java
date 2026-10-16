package com.example.backend.plaid;

import com.example.backend.investment.entity.AssetType;
import com.example.backend.plaid.dto.PlaidInvestmentHoldingDto;
import com.plaid.client.model.*;
import com.plaid.client.request.PlaidApi;
import org.junit.jupiter.api.Test;
import retrofit2.Call;
import retrofit2.Response;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PlaidInvestmentClientTest {
    @Test
    void joinsHoldingsWithSecuritiesAndPreservesNullableTicker() throws Exception {
        PlaidApi api = mock(PlaidApi.class);
        Call<InvestmentsHoldingsGetResponse> call = mock(Call.class);
        InvestmentsHoldingsGetResponse body = new InvestmentsHoldingsGetResponse()
            .holdings(List.of(new Holding().accountId("account").securityId("security")
                .quantity(0.5).institutionPrice(42.0).isoCurrencyCode("USD")))
            .securities(List.of(new Security().securityId("security").name("Fund").type("mutual fund")));
        when(api.investmentsHoldingsGet(any())).thenReturn(call);
        when(call.execute()).thenReturn(Response.success(body));
        List<PlaidInvestmentHoldingDto> result = new PlaidClient(api).getInvestmentHoldings("access");
        assertEquals(1, result.size());
        assertEquals(AssetType.MUTUAL_FUND, result.get(0).getAssetType());
        assertNull(result.get(0).getSymbol());
        assertEquals(0.5, result.get(0).getShares());
        assertEquals(42.0, result.get(0).getPrice());
        org.mockito.ArgumentCaptor<InvestmentsHoldingsGetRequest> request = org.mockito.ArgumentCaptor.forClass(InvestmentsHoldingsGetRequest.class);
        verify(api).investmentsHoldingsGet(request.capture());
        assertEquals("access", request.getValue().getAccessToken());
    }

    @Test
    void incompleteSnapshotFailsInsteadOfReturningAnEmptyPortfolio() throws Exception {
        PlaidApi api = mock(PlaidApi.class);
        Call<InvestmentsHoldingsGetResponse> call = mock(Call.class);
        when(api.investmentsHoldingsGet(any())).thenReturn(call);
        when(call.execute()).thenReturn(Response.success(new InvestmentsHoldingsGetResponse()
            .holdings(List.of(new Holding().accountId("account").securityId("missing")))
            .securities(List.of())));
        assertThrows(RuntimeException.class, () -> new PlaidClient(api).getInvestmentHoldings("access"));
    }
}
