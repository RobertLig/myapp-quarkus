package org.example.myapp.ws;

import jakarta.inject.Inject;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.example.myapp.service.PresenceService;

@ServerEndpoint("/ws/chat/{conversationId}/{userId}")
public class ChatWebSocket {

    @Inject
    ChatSessionRegistry registry;

    @Inject
    PresenceService presenceService;

    @Inject
    ChatBroadcaster broadcaster;

    @OnOpen
    public void onOpen(Session session,
                       @PathParam("conversationId") Long conversationId,
                       @PathParam("userId") Long userId) {

        session.getUserProperties().put("conversationId", conversationId);
        session.getUserProperties().put("userId", userId);

        registry.addSession(conversationId, session);

        // Presence: user is now online
        presenceService.setOnline(userId);
        broadcaster.broadcastPresence(userId, true);
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
