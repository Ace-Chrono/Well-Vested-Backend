    package com.example.backend.transaction.dto;

    import lombok.Getter;
    import lombok.NoArgsConstructor;
    import lombok.Setter;

    import java.time.LocalDate;

    @Getter
    @Setter
    @NoArgsConstructor
    public class TransactionFilterRequestDTO {
        private LocalDate startDate;
        private LocalDate endDate;
        private String merchantName;
        private String paymentChannel;
        private Double minAmount;
        private Double maxAmount;
        private String personalFinanceCategoryPrimary;
    }
