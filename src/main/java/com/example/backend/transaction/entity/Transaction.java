package com.example.backend.transaction.entity;

import com.example.backend.transaction.entity.embeddable.PersonalFinanceCategory;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID transactionId;
    private String plaidTransactionId;
    private String accountId;
    private Double amount;
    private String isoCurrencyCode;
    private LocalDate date;
    private String name;
    private String merchantName;
    private String originalDescription;
    private Boolean pending;
    private LocalDate authorizedDate;
    private String paymentChannel;

    @Embedded
    private PersonalFinanceCategory personalFinanceCategory;

    /* What your boilerplate would have looked like without Lombok
    public Transaction(){}

    Public Transaction(String transactionId, String accountId, Double amount, String isoCurrencyCode,
        LocalDate date, String name, String merchantName, String originalDescription, Boolean pending,
        LocalDate authorizedDate, String paymentChannel){
        this.transactionId = transactionId;
        ...
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    ...

     */
}

