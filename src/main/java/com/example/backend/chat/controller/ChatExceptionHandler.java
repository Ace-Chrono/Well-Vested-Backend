package com.example.backend.chat.controller;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.ResponseEntity;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.Map;

@RestControllerAdvice(assignableTypes = ChatController.class)
public class ChatExceptionHandler {
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleChatError(ResponseStatusException error, HttpServletRequest request) {
        return ResponseEntity.status(error.getStatusCode()).body(Map.of(
            "timestamp", Instant.now().toString(),
            "status", error.getStatusCode().value(),
            "error", "Chat request failed",
            "message", error.getReason() == null ? "Chat unavailable" : error.getReason(),
            "path", request.getRequestURI()));
    }
}
