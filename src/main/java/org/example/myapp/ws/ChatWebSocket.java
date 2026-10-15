package org.example.myapp.ws;

import jakarta.inject.Inject;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.example.myapp.service.AuthService;
import org.example.myapp.service.ConversationService;
import org.example.myapp.service.PresenceService;

@ServerEndpoint("/ws/chat/{conversationId}")
public class ChatWebSocket {

    @Inject
    ChatSessionRegistry registry;

    @Inject
    PresenceService presenceService;

    @Inject
    ChatBroadcaster broadcaster;

    @Inject
    AuthService authService;

    @Inject
    ConversationService conversationService;

    @OnOpen
    public void onOpen(Session session,
                       @PathParam("conversationId") Long conversationId) {

        // 1. Extract token from query parameters
        String token = null;
        var params = session.getRequestParameterMap();
        if (params.containsKey("token") && !params.get("token").isEmpty()) {
            token = params.get("token").get(0);
        }

        if (token == null || token.isBlank()) {
            close(session, "error.auth.missing.token");
            return;
        }

        // 2. Validate token and extract the real authenticated user ID
        Long userId;
        try {
            userId = authService.validateAndExtractUserId(token); // Replace with your actual token validation logic
        } catch (Exception e) {
            close(session, "error.auth.invalid.token");
            return;
        }

        // 3. Validate membership (ensure user actually belongs to this conversation)
        boolean isParticipant = conversationService.isUserInConversation(userId, conversationId);
        if (!isParticipant) {
            close(session, "error.websocket.notparticipant");
            return;
        }

        // 4. Store session metadata
        session.getUserProperties().put("conversationId", conversationId);
        session.getUserProperties().put("userId", userId);

        // 5. Register session
        registry.addSession(conversationId, session);

        // 6. Broadcast online presence
        presenceService.setOnline(userId);
        broadcaster.broadcastPresence(userId, true);
    }

    private void close(Session session, String reason) {
        try {
            session.close(new CloseReason(
                    CloseReason.CloseCodes.VIOLATED_POLICY,
                    reason
            ));
        } catch (Exception ignored) {}
    }

    @OnClose
    public void onClose(Session session) {
        Long conversationId = (Long) session.getUserProperties().get("conversationId");
        Long userId = (Long) session.getUserProperties().get("userId");

        if (conversationId != null && userId != null) {
            registry.removeSession(conversationId, session);

            presenceService.setOffline(userId);
            broadcaster.broadcastPresence(userId, false);
        }
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        // Handle error logging if needed
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        // Chat messages are sent via REST POST /messages,
        // but you can handle incoming WS control messages here if needed.
    }
}