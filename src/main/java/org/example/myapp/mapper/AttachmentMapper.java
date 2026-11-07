package org.example.myapp.mapper;

import org.example.myapp.dto.AttachmentDTO;
import org.example.myapp.model.MessageAttachment;

import java.util.function.Function;

public class AttachmentMapper {

    public static AttachmentDTO toDTO(MessageAttachment attachment) {
        return toDTO(attachment, Function.identity());
    }

    public static AttachmentDTO toDTO(MessageAttachment attachment, Function<String, String> readUrl) {
        AttachmentDTO dto = new AttachmentDTO();
        dto.id = attachment.getId();
        dto.userId = attachment.getUser().getId();
        dto.url = readUrl.apply(attachment.getUrl());
        dto.type = attachment.getType();
        dto.size = attachment.getSize();
        dto.createdAt = attachment.getCreatedAt();
        return dto;
    }
}
