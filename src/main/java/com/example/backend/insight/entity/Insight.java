package com.example.backend.insight.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.Instant;
import java.util.UUID;
import jakarta.persistence.*;

@Entity
@Table(name = "insights")
@Getter
@Setter
@NoArgsConstructor
public class Insight {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID insightId;
    private String scope;
    private String type;
    private String severity;
    private String subject;
    @Column(length = 2000)
    private String message;
    private String isoCurrencyCode;
    private LocalDate startDate;
    private LocalDate endDate;
    @Column(name = "metric_value")
    private Double value;
    private Double previousValue;
    private Double percentage;
    private Instant generatedAt;
}
