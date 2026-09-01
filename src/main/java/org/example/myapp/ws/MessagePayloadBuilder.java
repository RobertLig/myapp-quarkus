package org.example.myapp.ws;

import org.example.myapp.dto.MessageDTO;

public class MessagePayloadBuilder {

    public static String build(MessageDTO dto) {
        return """
        {
            "event": "MESSAGE",
            "id": %d,
            "senderId": %d,
            "content": "%s",
            "createdAt": %d
        }
        """.formatted(
                dto.id,
                dto.senderId,
                escape(dto.content),
                dto.createdAt.getTime()
        );
    }

    private static String escape(String s) {
        return s.replace("\"", "\\\"");
    }
}
