package org.example.myapp.dto;

import org.example.myapp.model.EventType;

public class MessageEventRequest {
    public Long conversationId;
    public Long userId;
    public Long messageId; // optional for typing
    public EventType type;

    public Long getConversationId() { return conversationId; }
}
