package com.example.backend.plaid.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "plaid_items")
@Getter
@Setter
public class PlaidItem {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false, unique = true)
  private String plaidItemId;

  @Column(nullable = false)
  private String accessToken;

  private String transactionCursor;
}