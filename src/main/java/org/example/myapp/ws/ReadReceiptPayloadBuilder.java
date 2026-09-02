package org.example.myapp.ws;

import org.example.myapp.model.MessageEvent;

public class ReadReceiptPayloadBuilder {

    public static String build(MessageEvent event) {
        return """
        {
            "event": "READ",
            "conversationId": %d,
            "userId": %d,
            "messageId": %d,
            "timestamp": %d,
            "unreadCount": 0
        }
        """.formatted(
                event.getMessage().getConversation().getId(),
                event.getUser().getId(),
                event.getMessage().getId(),
                event.getCreatedAt().getTime()
        );
    }
}
