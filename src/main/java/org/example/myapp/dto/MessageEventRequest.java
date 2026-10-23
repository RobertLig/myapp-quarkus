package org.example.myapp.dto;

import jakarta.validation.constraints.NotNull;
import org.example.myapp.model.EventType;

public class MessageEventRequest {
    @NotNull(message = "{error.conversation.required}")
    public Long conversationId;

    public Long messageId; // optional for typing

    @NotNull(message = "{error.type.required}")
    public EventType type;

    public Long getConversationId() { return conversationId; }
}
