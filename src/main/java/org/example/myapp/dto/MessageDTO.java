package org.example.myapp.dto;

import java.util.Date;
import java.util.List;

public class MessageDTO {
    public Long id;
    public Long conversationId;
    public Long senderId;
    public String content;
    public Date createdAt;
    public Date deletedAt;
    public Date deletedForRecipientAt;

    public List<ReactionDTO> reactions;
}

