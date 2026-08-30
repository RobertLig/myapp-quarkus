package org.example.myapp.mapper;

import org.example.myapp.dto.ConversationDTO;
import org.example.myapp.model.Conversation;

public class ConversationMapper {

    public static ConversationDTO toDTO(Conversation c) {
        ConversationDTO dto = new ConversationDTO();
        dto.id = c.getId();
        dto.createdAt = c.getCreatedAt();
        dto.participants = c.getParticipants().stream()
                .map(cp -> UserMapper.toDTO(cp.getUser()))
                .toList();
        return dto;
    }
}
