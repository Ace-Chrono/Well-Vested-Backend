package com.example.backend.chat;

import com.example.backend.chat.entity.ChatTurn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface ChatRepository extends JpaRepository<ChatTurn, UUID> {
    List<ChatTurn> findTop6ByConversationIdOrderByCreatedAtDesc(UUID conversationId);
}
