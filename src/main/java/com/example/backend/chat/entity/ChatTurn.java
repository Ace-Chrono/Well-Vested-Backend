package com.example.backend.chat.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.util.UUID;
import java.time.Instant;
import java.time.LocalDate;
import jakarta.persistence.*;

@Entity
@Table(name = "chat_turns")
@Getter
@Setter
@NoArgsConstructor
public class ChatTurn {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID turnId;
    private UUID conversationId;
    @Column(length = 4000)
    private String message;
    @Column(length = 24000)
    private String reply;
    private String currency;
    private LocalDate startDate;
    private LocalDate endDate;
    private Instant createdAt;
}
