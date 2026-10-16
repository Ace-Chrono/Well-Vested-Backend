package com.example.backend.chat.service;

import com.example.backend.chat.dto.ChatRequestDTO;
import com.example.backend.chat.dto.ChatResponseDTO;
import java.util.List;
import java.util.UUID;

public interface ChatService {
    ChatResponseDTO sendMessage(ChatRequestDTO request);
    List<ChatResponseDTO> getHistory(UUID conversationId);
}
