package org.example.myapp.ws;

import jakarta.websocket.RemoteEndpoint;
import jakarta.websocket.SendHandler;
import jakarta.websocket.Session;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ChatBroadcasterTest {

    @Test
    void broadcastSendsMessagesAsynchronously() {
        ChatSessionRegistry registry = mock(ChatSessionRegistry.class);
        ChatBroadcaster broadcaster = new ChatBroadcaster();
        broadcaster.registry = registry;

        Session session = mock(Session.class);
        RemoteEndpoint.Async remote = mock(RemoteEndpoint.Async.class);
        when(registry.getSessions(1L)).thenReturn(Set.of(session));
        when(session.isOpen()).thenReturn(true);
        when(session.getAsyncRemote()).thenReturn(remote);

        broadcaster.broadcast(1L, "{\"event\":\"PRESENCE\"}");

        verify(remote).sendText(eq("{\"event\":\"PRESENCE\"}"), any(SendHandler.class));
        verify(session, never()).getBasicRemote();
    }
}
