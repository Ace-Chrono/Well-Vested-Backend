package com.example.backend.plaid.controller;

import com.example.backend.plaid.PlaidClient;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/test/plaid")
@RequiredArgsConstructor
public class PlaidTestController {

  private final PlaidClient plaidClient;

  /**
   * Sandbox-only helper.
   *
   * Creates a Plaid Sandbox Item and returns its temporary public token.
   * The public token should then be sent through the application's
   * real token-exchange endpoint.
   */
  @PostMapping("/public-token")
  public Map<String, String> createSandboxPublicToken() {

    String publicToken =
        plaidClient.createSandboxPublicToken();

    return Map.of(
        "publicToken", publicToken
    );
  }
}