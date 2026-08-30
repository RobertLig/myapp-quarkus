package org.example.myapp.mapper;

import org.example.myapp.dto.MessageDTO;
import org.example.myapp.model.Message;

public class MessageMapper {

    public static MessageDTO toDTO(Message m) {
        MessageDTO dto = new MessageDTO();
        dto.id = m.getId();
        dto.senderId = m.getSender().getId();
        dto.content = m.getContent();
        dto.createdAt = m.getCreatedAt();
        return dto;
    }
}
