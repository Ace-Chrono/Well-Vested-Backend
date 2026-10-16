package com.example.backend.chat;

import com.example.backend.chat.client.OpenAiClient;
import com.example.backend.chat.dto.ChatRequestDTO;
import com.example.backend.chat.entity.ChatTurn;
import com.example.backend.chat.mapper.ChatMapper;
import com.example.backend.chat.service.ChatContextService;
import com.example.backend.chat.service.impls.ChatServiceImpl;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

class ChatServiceImplTest {
    private final ChatRepository repository = mock(ChatRepository.class);
    private final ChatContextService context = mock(ChatContextService.class);
    private final OpenAiClient client = mock(OpenAiClient.class);
    private final ChatServiceImpl service = new ChatServiceImpl(repository, Mappers.getMapper(ChatMapper.class), context, client);

    @Test
    void sendsFinancialContextAndHistoryAndPersistsReply() {
        UUID conversationId = UUID.randomUUID();
        ChatRequestDTO request = new ChatRequestDTO();
        request.setConversationId(conversationId);
        request.setMessage("Explain my spending");
        ChatTurn prior = new ChatTurn(); prior.setMessage("Hello"); prior.setReply("Hi");
        when(repository.findTop6ByConversationIdOrderByCreatedAtDesc(conversationId)).thenReturn(List.of(prior));
        when(context.buildContext(request)).thenReturn("{\"insights\":[],\"transactions\":[],\"holdings\":[]}");
        when(client.reply(anyList())).thenAnswer(call -> {
            List<Map<String, String>> input = call.getArgument(0);
            assertTrue(input.get(0).get("content").contains("holdings"));
            assertEquals("Hello", input.get(1).get("content"));
            assertEquals("assistant", input.get(2).get("role"));
            assertEquals(request.getMessage(), input.get(3).get("content"));
            return "No saved insights yet.";
        });
        when(repository.save(any(ChatTurn.class))).thenAnswer(call -> {
            ChatTurn turn = call.getArgument(0); turn.setTurnId(UUID.randomUUID()); return turn;
        });
        var result = service.sendMessage(request);
        assertEquals(conversationId, result.getConversationId());
        assertEquals("No saved insights yet.", result.getReply());
        assertNotNull(result.getTurnId()); assertNotNull(result.getCreatedAt());
    }

    @Test
    void failedAiCallDoesNotSaveExchange() {
        ChatRequestDTO request = new ChatRequestDTO(); request.setMessage("Hi");
        when(context.buildContext(request)).thenReturn("{}");
        when(client.reply(anyList())).thenThrow(new RuntimeException("Unavailable"));
        assertThrows(RuntimeException.class, () -> service.sendMessage(request));
        verify(repository, never()).save(any());
    }

    @Test
    void rejectsInvalidDatesBeforeGatheringData() {
        ChatRequestDTO request = new ChatRequestDTO(); request.setStartDate(request.getEndDate().plusDays(1));
        assertThrows(ResponseStatusException.class, () -> service.sendMessage(request));
        verifyNoInteractions(repository, context, client);
    }

    @Test
    void validatesMessageLengthAndBlankInput() {
        try (var factory = jakarta.validation.Validation.buildDefaultValidatorFactory()) {
            ChatRequestDTO request = new ChatRequestDTO(); request.setMessage(" ");
            assertFalse(factory.getValidator().validate(request).isEmpty());
            request.setMessage("a".repeat(4001));
            assertFalse(factory.getValidator().validate(request).isEmpty());
        }
    }
}
