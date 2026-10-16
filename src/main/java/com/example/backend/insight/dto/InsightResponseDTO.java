package com.example.backend.insight.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class InsightResponseDTO {
    private UUID insightId;
    private String scope;
    private String type;
    private String severity;
    private String subject;
    private String message;
    private String isoCurrencyCode;
    private LocalDate startDate;
    private LocalDate endDate;
    private Double value;
    private Double previousValue;
    private Double percentage;
    private Instant generatedAt;
}
