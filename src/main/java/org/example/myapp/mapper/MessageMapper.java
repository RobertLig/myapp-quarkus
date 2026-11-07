package org.example.myapp.mapper;

import org.example.myapp.dto.MessageDTO;
import org.example.myapp.model.Message;

import java.util.function.Function;

public class MessageMapper {

    public static MessageDTO toDTO(Message message) {
        return toDTO(message, Function.identity());
    }

    public static MessageDTO toDTO(Message message, Function<String, String> readUrl) {
        MessageDTO dto = new MessageDTO();
        dto.id = message.getId();
        dto.conversationId = message.getConversation().getId();
        dto.senderId = message.getSender() != null ? message.getSender().getId() : null;
        dto.content = message.getContent();
        dto.createdAt = message.getCreatedAt();
        dto.deletedAt = message.getDeletedAt();
        dto.deletedForRecipientAt = message.getDeletedForRecipientAt();

        dto.reactions = message.getReactions().stream()
                .map(ReactionMapper::toDTO)
                .toList();

        dto.attachments = message.getAttachments().stream()
                .map(attachment -> AttachmentMapper.toDTO(attachment, readUrl))
                .toList();

        return dto;
    }
}
