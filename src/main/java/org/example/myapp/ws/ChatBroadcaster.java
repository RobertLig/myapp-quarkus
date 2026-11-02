package org.example.myapp.ws;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.websocket.Session;
import org.example.myapp.repository.UserBlockRepository;
import org.jboss.logging.Logger;

@ApplicationScoped
public class ChatBroadcaster {

    private static final Logger log = Logger.getLogger(ChatBroadcaster.class);

    @Inject
    ChatSessionRegistry registry;

    @Inject
    UserBlockRepository userBlockRepository;

    public void broadcast(Long conversationId, String jsonPayload) {

        System.out.println("Broadcasting to " + registry.getSessions(conversationId).size() + " sessions for conversation " + conversationId);

        for (Session session : registry.getSessions(conversationId)) {
            send(session, jsonPayload);
        }
    }

    public void broadcastPresence(Long userId, boolean online) {
        String json = presencePayload(userId, online);
        for (Session session : registry.getAllSessions()) {
            Long recipientId = (Long) session.getUserProperties().get("userId");
            if (recipientId == null || !userBlockRepository.exists(recipientId, userId)) {
                send(session, json);
            }
        }
    }

    public void broadcastPresenceToUser(Long userId, boolean online, Long recipientId) {
        String json = presencePayload(userId, online);
        for (Session session : registry.getSessionsByUserId(recipientId)) {
            send(session, json);
        }
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
                session.getAsyncRemote().sendText(jsonPayload, result -> {
                    if (!result.isOK()) {
                        log.warn("Failed to send WebSocket message to session " + session.getId(),
                                result.getException());
                    }
                });
            } catch (IllegalStateException e) {
                log.warn("Failed to send WebSocket message to session " + session.getId(), e);
            }
        }
    }

    private String presencePayload(Long userId, boolean online) {
        return """
                {
                    "event": "PRESENCE",
                    "userId": %d,
                    "online": %b
                }
                """.formatted(userId, online);
    }
}
