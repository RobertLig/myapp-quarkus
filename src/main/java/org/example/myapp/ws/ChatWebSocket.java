package org.example.myapp.ws;

import jakarta.inject.Inject;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;

@ServerEndpoint("/ws/chat/{conversationId}/{userId}")
public class ChatWebSocket {

    @Inject
    ChatSessionRegistry registry;

    @OnOpen
    public void onOpen(Session session,
                       @PathParam("conversationId") Long conversationId,
                       @PathParam("userId") Long userId) {

        session.getUserProperties().put("conversationId", conversationId);
        session.getUserProperties().put("userId", userId);

        registry.addSession(conversationId, session);
    }

    @OnClose
    public void onClose(Session session) {
        Long conversationId = (Long) session.getUserProperties().get("conversationId");
        registry.removeSession(conversationId, session);
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
