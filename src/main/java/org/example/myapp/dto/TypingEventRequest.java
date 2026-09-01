package org.example.myapp.dto;

import org.example.myapp.model.EventType;

public class TypingEventRequest {
    public Long conversationId;
    public Long userId;
    public EventType type; // TYPING_START or TYPING_STOP
}
