package com.example.backend.plaid.controller;

import com.example.backend.plaid.service.PlaidService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/plaid")
@RequiredArgsConstructor
public class PlaidController {

  private final PlaidService plaidService;

  @PostMapping("/link-token")
  public Map<String, String> createLinkToken() {

    // Temporary single-user ID.
    // When authentication exists, this should come from
    // the authenticated user.
    String linkToken =
        plaidService.createLinkToken("portfolio-demo-user");

    return Map.of("linkToken", linkToken);
  }

  @PostMapping("/exchange")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void exchangePublicToken(
      @RequestParam String publicToken
  ) {
    plaidService.exchangePublicToken(publicToken);
  }
}