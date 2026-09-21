package com.example.backend.plaid.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PlaidTokenExchangeDto {

  private String accessToken;
  private String itemId;
}