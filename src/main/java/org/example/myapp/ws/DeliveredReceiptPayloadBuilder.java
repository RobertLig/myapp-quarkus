package org.example.myapp.ws;

import org.example.myapp.model.MessageEvent;

public class DeliveredReceiptPayloadBuilder {

    public static String build(MessageEvent event) {
        return """
        {
            "event": "DELIVERED",
            "conversationId": %d,
            "userId": %d,
            "messageId": %d,
            "timestamp": %d
        }
        """.formatted(
                event.getMessage().getConversation().getId(),
                event.getUser().getId(),
                event.getMessage().getId(),
                event.getCreatedAt().getTime()
        );
    }
}
