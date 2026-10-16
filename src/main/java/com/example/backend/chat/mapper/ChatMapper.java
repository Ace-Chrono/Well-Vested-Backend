package com.example.backend.chat.mapper;

import com.example.backend.chat.entity.ChatTurn;
import com.example.backend.chat.dto.ChatResponseDTO;
import org.mapstruct.Mapper;
import java.util.List;

@Mapper(componentModel = "spring")
public interface ChatMapper {
    ChatResponseDTO toResponseDTO(ChatTurn entity);
    List<ChatResponseDTO> toResponseDTOList(List<ChatTurn> turns);
}
