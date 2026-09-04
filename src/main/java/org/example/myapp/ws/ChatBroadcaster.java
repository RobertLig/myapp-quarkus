package org.example.myapp.ws;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.websocket.Session;

import java.io.IOException;

@ApplicationScoped
public class ChatBroadcaster {

    @Inject
    ChatSessionRegistry registry;

    public void broadcast(Long conversationId, String jsonPayload) {

        for (Session session : registry.getSessions(conversationId)) {
            if (session.isOpen()) {
                try {
                    session.getBasicRemote().sendText(jsonPayload);
                } catch (IOException ignored) {
                    // optional logging
                }
            }
        }
    }

    public void broadcastPresence(Long userId, boolean online) {

        String json = """
    {
        "event": "PRESENCE",
        "userId": %d,
        "online": %b
    }
    """.formatted(userId, online);

        broadcastToAll(json);
    }

    public void broadcastToAll(String jsonPayload) {
        for (Session session : registry.getAllSessions()) {
            if (session.isOpen()) {
                try {
                    session.getBasicRemote().sendText(jsonPayload);
                } catch (IOException ignored) {
                    // optional logging
                }
            }
        }
    }
}
