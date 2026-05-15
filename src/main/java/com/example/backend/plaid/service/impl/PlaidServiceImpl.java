package com.example.backend.plaid.service.impl;

import com.example.backend.plaid.PlaidClient;
import com.example.backend.plaid.dto.PlaidAccountDto;
import com.example.backend.plaid.dto.PlaidInvestmentHoldingDto;
import com.example.backend.plaid.dto.PlaidSecurityDto;
import com.example.backend.plaid.dto.PlaidTransactionDto;
import com.example.backend.plaid.service.PlaidService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

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
    public List<PlaidAccountDto> getAccounts(String accessToken) {
        return plaidClient.fetchAccounts(accessToken);
    }

    @Override
    public List<PlaidTransactionDto> getTransactions(
            String accessToken,
            String startDate,
            String endDate
    ) {
        return plaidClient.fetchTransactions(accessToken, startDate, endDate);
    }

    @Override
    public List<PlaidInvestmentHoldingDto> getInvestmentHoldings(String accessToken) {
        return plaidClient.fetchHoldings(accessToken);
    }

    @Override
    public List<PlaidSecurityDto> getSecurities(String accessToken) {
        return plaidClient.fetchSecurities(accessToken);
    }

    @Override
    public void removeItem(String accessToken) {
        plaidClient.removeItem(accessToken);
    }
}
