package org.example.myapp.mapper;

import org.example.myapp.dto.MessageEventResponse;
import org.example.myapp.model.MessageEvent;

public class MessageEventMapper {

    public static MessageEventResponse toDTO(MessageEvent e) {
        MessageEventResponse dto = new MessageEventResponse();
        dto.id = e.getId();
        dto.type = e.getType();
        dto.userId = e.getUser().getId();
        dto.messageId = e.getMessage() != null ? e.getMessage().getId() : null;
        dto.createdAt = e.getCreatedAt();
        return dto;
    }
}
