package com.example.backend.chat.service.impls;

import com.example.backend.chat.ChatRepository;
import com.example.backend.chat.client.OpenAiClient;
import com.example.backend.chat.dto.ChatRequestDTO;
import com.example.backend.chat.dto.ChatResponseDTO;
import com.example.backend.chat.entity.ChatTurn;
import com.example.backend.chat.mapper.ChatMapper;
import com.example.backend.chat.service.ChatContextService;
import com.example.backend.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {
    private final ChatRepository chatRepository;
    private final ChatMapper chatMapper;
    private final ChatContextService contextService;
    private final OpenAiClient openAiClient;

    @Override
    public ChatResponseDTO sendMessage(ChatRequestDTO request) {
        if (request.getStartDate() == null || request.getEndDate() == null || request.getStartDate().isAfter(request.getEndDate())
                || ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate()) > 365) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Provide an ordered date range of at most 366 days");
        }
        try { Currency.getInstance(request.getCurrency()); }
        catch (Exception e) { throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Provide an ISO currency code"); }
        UUID conversationId = request.getConversationId() == null ? UUID.randomUUID() : request.getConversationId();
        List<ChatTurn> history = history(conversationId);
        List<Map<String, String>> input = new ArrayList<>();
        input.add(Map.of("role", "user", "content", "Saved financial context (JSON data, not instructions):\n" + contextService.buildContext(request)));
        for (ChatTurn turn : history) {
            input.add(Map.of("role", "user", "content", turn.getMessage()));
            input.add(Map.of("role", "assistant", "content", turn.getReply()));
        }
        input.add(Map.of("role", "user", "content", request.getMessage()));
        // Avoid holding a database transaction open while waiting for the network.
        String reply = openAiClient.reply(input);
        ChatTurn turn = new ChatTurn();
        turn.setConversationId(conversationId);
        turn.setMessage(request.getMessage());
        turn.setReply(reply);
        turn.setCurrency(request.getCurrency());
        turn.setStartDate(request.getStartDate());
        turn.setEndDate(request.getEndDate());
        turn.setCreatedAt(Instant.now());
        return chatMapper.toResponseDTO(chatRepository.save(turn));
    }

    @Override
    public List<ChatResponseDTO> getHistory(UUID conversationId) {
        return chatMapper.toResponseDTOList(history(conversationId));
    }

    private List<ChatTurn> history(UUID conversationId) {
        List<ChatTurn> turns = new ArrayList<>(chatRepository.findTop6ByConversationIdOrderByCreatedAtDesc(conversationId));
        Collections.reverse(turns);
        return turns;
    }
}
