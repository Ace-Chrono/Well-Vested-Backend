package com.example.backend.chat.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.util.UUID;
import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class ChatResponseDTO {
    private UUID turnId;
    private UUID conversationId;
    private String message;
    private String reply;
    private String currency;
    private LocalDate startDate;
    private LocalDate endDate;
    private Instant createdAt;
}
