package com.example.backend.investment;

import com.example.backend.investment.entity.Investment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.List;

@Repository
public interface InvestmentRepository extends JpaRepository<Investment, UUID> {
    List<Investment> findByPlaidItemId(String plaidItemId);
    void deleteByPlaidItemIdIsNotNull();
}
