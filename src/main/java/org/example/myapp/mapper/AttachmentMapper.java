package org.example.myapp.mapper;

import org.example.myapp.dto.AttachmentDTO;
import org.example.myapp.model.MessageAttachment;

public class AttachmentMapper {

    public static AttachmentDTO toDTO(MessageAttachment attachment) {
        AttachmentDTO dto = new AttachmentDTO();
        dto.id = attachment.getId();
        dto.userId = attachment.getUser().getId();
        dto.url = attachment.getUrl();
        dto.type = attachment.getType();
        dto.size = attachment.getSize();
        dto.createdAt = attachment.getCreatedAt();
        return dto;
    }
}
