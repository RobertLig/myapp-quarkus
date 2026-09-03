package org.example.myapp.mapper;

import org.example.myapp.dto.ReactionDTO;
import org.example.myapp.model.MessageReaction;

public class ReactionMapper {

    public static ReactionDTO toDTO(MessageReaction reaction) {
        ReactionDTO dto = new ReactionDTO();
        dto.id = reaction.getId();
        dto.userId = reaction.getUser().getId();
        dto.reaction = reaction.getReaction();
        dto.createdAt = reaction.getCreatedAt();
        return dto;
    }
}
