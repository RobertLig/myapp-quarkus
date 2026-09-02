package org.example.myapp.ws;

import org.example.myapp.model.Message;
import org.example.myapp.model.User;

import java.util.Date;

public class ReactionPayloadBuilder {

    public static String build(Message message, User user, String reaction) {
        return """
        {
            "event": "REACTION",
            "conversationId": %d,
            "messageId": %d,
            "userId": %d,
            "reaction": %s,
            "timestamp": %d
        }
        """.formatted(
                message.getConversation().getId(),
                message.getId(),
                user.getId(),
                reaction == null ? "null" : "\"" + reaction + "\"",
                new Date().getTime()
        );
    }
}

