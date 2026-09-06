package org.example.myapp.ws;

import jakarta.inject.Inject;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.example.myapp.service.AuthService;
import org.example.myapp.service.ConversationService;
import org.example.myapp.service.PresenceService;

@ServerEndpoint("/ws/chat/{conversationId}/{userId}")
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
                       @PathParam("conversationId") Long conversationId,
                       @PathParam("userId") Long userId) {

        // Extract token
        String token = null;
        var params = session.getRequestParameterMap();
        if (params.containsKey("token") && !params.get("token").isEmpty()) {
            token = params.get("token").get(0);
        }

        if (token == null || token.isBlank()) {
            close(session, "error.auth.missing.token");
            return;
        }

        // Validate token
        Long authenticatedUserId;
        try {
            authenticatedUserId = authService.validateAndExtractUserId(token);
        } catch (Exception e) {
            close(session, "error.auth.invalid.token");
            return;
        }

        // Prevent impersonation
        if (!authenticatedUserId.equals(userId)) {
            close(session, "error.auth.user.mismatch");
            return;
        }

        // Validate membership
        boolean isParticipant = conversationService
                .listConversations(userId)
                .stream()
                .anyMatch(cp -> cp.getConversation().getId().equals(conversationId));

        if (!isParticipant) {
            close(session, "error.websocket.notparticipant");
            return;
        }

        // Store session metadata
        session.getUserProperties().put("conversationId", conversationId);
        session.getUserProperties().put("userId", userId);

        // Register session
        registry.addSession(conversationId, session);

        // Presence
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

        registry.removeSession(conversationId, session);

        // Presence: user is now offline
        presenceService.setOffline(userId);
        broadcaster.broadcastPresence(userId, false);
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        // optional logging
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        // optional: echo or ignore
    }
}
