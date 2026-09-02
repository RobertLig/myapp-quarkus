package org.example.myapp.ws;

import org.example.myapp.model.MessageEvent;

public class DeletedEventPayloadBuilder {

    public static String build(MessageEvent event) {
        return """
        {
            "event": "DELETED",
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
