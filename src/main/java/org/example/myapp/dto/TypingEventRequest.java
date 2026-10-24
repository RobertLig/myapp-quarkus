package org.example.myapp.dto;

import jakarta.validation.constraints.NotNull;
import org.example.myapp.model.EventType;
import org.example.myapp.validation.TypingEventType;

public class TypingEventRequest {
    @NotNull(message = "{error.conversation.required}")
    public Long conversationId;

    @NotNull(message = "{error.type.required}")
    @TypingEventType
    public EventType type; // TYPING_START or TYPING_STOP
}
