package org.example.myapp.mapper;

import org.example.myapp.dto.ConversationDTO;
import org.example.myapp.dto.ConversationListItemDTO;
import org.example.myapp.model.Conversation;
import org.example.myapp.model.ConversationParticipant;

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

    public static ConversationListItemDTO toListItemDTO(ConversationParticipant cp) {
        Conversation c = cp.getConversation();

        ConversationListItemDTO dto = new ConversationListItemDTO();
        dto.id = c.getId();
        dto.createdAt = c.getCreatedAt();
        dto.deletedAt = cp.getDeletedAt();

        dto.participants = c.getParticipants().stream()
                .map(p -> UserMapper.toDTO(p.getUser()))
                .toList();

        dto.unreadCount = cp.getUnreadCount();

        return dto;
    }
}
