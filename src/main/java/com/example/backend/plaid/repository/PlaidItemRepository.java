package com.example.backend.plaid.repository;

import com.example.backend.plaid.entity.PlaidItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PlaidItemRepository
    extends JpaRepository<PlaidItem, UUID> {

  Optional<PlaidItem> findByPlaidItemId(String plaidItemId);
  Optional<PlaidItem> findFirstBy();
}