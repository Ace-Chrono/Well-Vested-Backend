package com.example.backend.investment.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "investments", uniqueConstraints = @UniqueConstraint(
    columnNames = {"plaidItemId", "accountId", "securityId"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Investment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID investmentId;
    private String symbol;
    private String name;
    @Enumerated(EnumType.STRING)
    private AssetType assetType;
    private Double shares;
    private Double price;
    private Double dayChange;
    @Column(name = "plaidItemId")
    private String plaidItemId;
    @Column(name = "accountId")
    private String accountId;
    @Column(name = "securityId")
    private String securityId;
    private String isoCurrencyCode;
}
