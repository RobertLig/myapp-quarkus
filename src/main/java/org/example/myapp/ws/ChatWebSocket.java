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
            close(session, "error.auth.missing.token");
            return;
        }

        Long userId;
        try {
            userId = authService.validateAndExtractUserId(token);
        } catch (Exception e) {
            close(session, "error.auth.invalid.token");
            return;
        }

        // Run the blocking database check safely off the IO thread
        try {
            Long finalUserId = userId;
            boolean isParticipant = java.util.concurrent.CompletableFuture.supplyAsync(() ->
                    conversationService.isUserInConversation(finalUserId, conversationId)
            ).get(); // .get() blocks this specific worker wrapper safely without freezing the Netty IO event loop

            if (!isParticipant) {
                close(session, "error.websocket.notparticipant");
                return;
            }

            session.getUserProperties().put("conversationId", conversationId);
            session.getUserProperties().put("userId", finalUserId);

            registry.addSession(conversationId, session);

            presenceService.setOnline(finalUserId);
            broadcaster.broadcastPresence(finalUserId, true);

            System.out.println("-> SUCCESS: Client session registered for user " + finalUserId);

        } catch (Exception e) {
            e.printStackTrace();
            close(session, "error.database.error");
        }
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