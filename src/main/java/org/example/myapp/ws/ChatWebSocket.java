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

        System.out.println("=== WS onOpen triggered for conversation: " + conversationId + " ===");

        String token = null;
        var params = session.getRequestParameterMap();
        if (params.containsKey("token") && !params.get("token").isEmpty()) {
            token = params.get("token").get(0);
        }

        if (token == null || token.isBlank()) {
            System.out.println("-> FAILED: Missing token");
            close(session, "error.auth.missing.token");
            return;
        }

        Long userId;
        try {
            userId = authService.validateAndExtractUserId(token);
            System.out.println("-> Token valid. Extracted userId: " + userId);
        } catch (Exception e) {
            System.out.println("-> FAILED: Token validation threw exception: " + e.getMessage());
            close(session, "error.auth.invalid.token");
            return;
        }

        // Offload the blocking, transactional database query to a worker thread safely
        boolean isParticipant = false;
        try {
            Long finalUserId = userId;
            isParticipant = java.util.concurrent.CompletableFuture.supplyAsync(() ->
                    conversationService.isUserInConversation(finalUserId, conversationId)
            ).get(); // Blocks only this background worker thread, keeping the IO event loop free

            System.out.println("-> Is user " + userId + " participant in conversation " + conversationId + "? " + isParticipant);
        } catch (Exception e) {
            System.out.println("-> FAILED: Database error: " + e.getMessage());
            e.printStackTrace();
            close(session, "error.database.error");
            return;
        }

        if (!isParticipant) {
            System.out.println("-> FAILED: User is not a participant");
            close(session, "error.websocket.notparticipant");
            return;
        }

        // Store session metadata & register
        session.getUserProperties().put("conversationId", conversationId);
        session.getUserProperties().put("userId", userId);

        registry.addSession(conversationId, userId, session);
        System.out.println("-> SUCCESS: Client session registered! Total sessions for conv " + conversationId + " = " + registry.getSessions(conversationId).size());

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

        if (conversationId != null && userId != null) {
            registry.removeSession(conversationId, userId, session);

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