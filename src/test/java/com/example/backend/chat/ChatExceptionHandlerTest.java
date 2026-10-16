package com.example.backend.chat;

import com.example.backend.chat.controller.ChatExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import static org.junit.jupiter.api.Assertions.*;

class ChatExceptionHandlerTest {
    @Test
    void includesSafeReasonInPostmanResponse() {
        var result = new ChatExceptionHandler().handleChatError(
            new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Chat is not configured: set OPENAI_API_KEY on the backend"),
            new MockHttpServletRequest("POST", "/api/chat"));
        assertEquals(503, result.getStatusCode().value());
        assertTrue(result.getBody().get("message").toString().contains("OPENAI_API_KEY"));
        assertEquals("/api/chat", result.getBody().get("path"));
    }
}
