package com.example.backend.chat.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class ChatRequestDTO {
    @NotBlank
    @Size(max = 4000)
    private String message;
    private UUID conversationId;
    @NotBlank
    @Pattern(regexp = "[A-Z]{3}")
    private String currency = "USD";
    @NotNull
    private LocalDate startDate = LocalDate.now(java.time.ZoneId.of("America/New_York")).minusDays(29);
    @NotNull
    private LocalDate endDate = LocalDate.now(java.time.ZoneId.of("America/New_York"));
}
