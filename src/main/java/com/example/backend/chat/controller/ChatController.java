package com.example.backend.chat.controller;

import com.example.backend.chat.dto.ChatRequestDTO;
import com.example.backend.chat.dto.ChatResponseDTO;
import com.example.backend.chat.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;

    @PostMapping
    public ChatResponseDTO sendMessage(@Valid @RequestBody ChatRequestDTO request) {
        return chatService.sendMessage(request);
    }

    @GetMapping("/{conversationId}")
    public List<ChatResponseDTO> getHistory(@PathVariable UUID conversationId) {
        return chatService.getHistory(conversationId);
    }
}
