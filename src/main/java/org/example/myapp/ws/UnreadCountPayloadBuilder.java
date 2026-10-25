package org.example.myapp.ws;

import org.example.myapp.model.ConversationParticipant;

public class UnreadCountPayloadBuilder {

    public static String build(ConversationParticipant cp) {
        return """
        {
            "event": "UNREAD_COUNT_UPDATE",
            "conversationId": %d,
            "userId": %d,
            "unreadCount": %d
        }
        """.formatted(
                cp.getConversation().getId(),
                cp.getUser().getId(),
                cp.getUnreadCount()
        );
    }
}
