package org.example.myapp.mapper;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.example.myapp.dto.ConversationDTO;
import org.example.myapp.dto.ConversationListItemDTO;
import org.example.myapp.model.Conversation;
import org.example.myapp.model.ConversationParticipant;
import org.example.myapp.model.User;
import org.example.myapp.service.PresenceService;

@ApplicationScoped
public class ConversationMapper {

    @Inject
    PresenceService presenceService;

    public static ConversationDTO toDTO(Conversation c) {
        ConversationDTO dto = new ConversationDTO();
        dto.id = c.getId();
        dto.createdAt = c.getCreatedAt();
        dto.participants = c.getParticipants().stream()
                .map(cp -> UserMapper.toDTO(cp.getUser()))
                .toList();
        return dto;
    }

    public ConversationListItemDTO toListItemDTO(ConversationParticipant cp) {
        Conversation c = cp.getConversation();

        ConversationListItemDTO dto = new ConversationListItemDTO();
        dto.id = c.getId();
        dto.createdAt = c.getCreatedAt();
        dto.deletedAt = cp.getDeletedAt();

        dto.participants = c.getParticipants().stream()
                .map(p -> UserMapper.toDTO(p.getUser()))
                .toList();

        dto.unreadCount = cp.getUnreadCount();

        User otherUser = cp.getConversation()
                .getParticipants()
                .stream()
                .map(ConversationParticipant::getUser)
                .filter(u -> !u.getId().equals(cp.getUser().getId()))
                .findFirst()
                .orElse(null);

        if (otherUser != null) {
            dto.otherUserOnline = presenceService.isOnline(otherUser.getId());
        }

        return dto;
    }
}
