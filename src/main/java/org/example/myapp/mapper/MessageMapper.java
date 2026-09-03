package org.example.myapp.mapper;

import org.example.myapp.dto.MessageDTO;
import org.example.myapp.model.Message;

public class MessageMapper {

    public static MessageDTO toDTO(Message message) {
        MessageDTO dto = new MessageDTO();
        dto.id = message.getId();
        dto.conversationId = message.getConversation().getId();
        dto.senderId = message.getSender().getId();
        dto.content = message.getContent();
        dto.createdAt = message.getCreatedAt();
        dto.deletedAt = message.getDeletedAt();
        dto.deletedForRecipientAt = message.getDeletedForRecipientAt();

        dto.reactions = message.getReactions().stream()
                .map(ReactionMapper::toDTO)
                .toList();

        dto.attachments = message.getAttachments().stream()
                .map(AttachmentMapper::toDTO)
                .toList();

        return dto;
    }
}
