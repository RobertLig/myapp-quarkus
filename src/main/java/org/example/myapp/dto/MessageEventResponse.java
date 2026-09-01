package org.example.myapp.dto;

import org.example.myapp.model.EventType;
import java.util.Date;

public class MessageEventResponse {
    public Long id;
    public EventType type;
    public Long userId;
    public Long messageId;
    public Date createdAt;
}
