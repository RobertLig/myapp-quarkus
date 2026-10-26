package org.example.myapp.ws;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.websocket.Session;
import org.jboss.logging.Logger;

import java.io.IOException;

@ApplicationScoped
public class ChatBroadcaster {

    private static final Logger log = Logger.getLogger(ChatBroadcaster.class);

    @Inject
    ChatSessionRegistry registry;

    public void broadcast(Long conversationId, String jsonPayload) {

        System.out.println("Broadcasting to " + registry.getSessions(conversationId).size() + " sessions for conversation " + conversationId);

        for (Session session : registry.getSessions(conversationId)) {
            send(session, jsonPayload);
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
            send(session, jsonPayload);
        }
    }

    public void broadcastToUser(Long userId, String jsonPayload) {
        for (Session session : registry.getSessionsByUserId(userId)) {
            send(session, jsonPayload);
        }
    }

    private void send(Session session, String jsonPayload) {
        synchronized (session) {
            if (!session.isOpen()) {
                return;
            }

            try {
                session.getBasicRemote().sendText(jsonPayload);
            } catch (IOException | IllegalStateException e) {
                log.warn("Failed to send WebSocket message to session " + session.getId(), e);
            }
        }
    }
}
