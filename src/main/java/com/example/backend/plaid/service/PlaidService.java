package com.example.backend.plaid.service;

import com.example.backend.plaid.dto.PlaidAccountDto;
import com.example.backend.plaid.dto.PlaidInvestmentHoldingDto;
import com.example.backend.plaid.dto.PlaidSecurityDto;
import com.example.backend.plaid.dto.PlaidTransactionDto;
import java.util.List;

public interface PlaidService {
    String createLinkToken(String userId);
    String exchangePublicToken(String publicToken);
    List<PlaidAccountDto> getAccounts(String accessToken);
    List<PlaidTransactionDto> getTransactions(String accessToken, String startDate, String endDate);
    List<PlaidInvestmentHoldingDto> getInvestmentHoldings(String accessToken);
    List<PlaidSecurityDto> getSecurities(String accessToken);
    void removeItem(String accessToken);
}
