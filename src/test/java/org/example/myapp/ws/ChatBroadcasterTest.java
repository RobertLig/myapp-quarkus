package org.example.myapp.ws;

import jakarta.websocket.RemoteEndpoint;
import jakarta.websocket.SendHandler;
import jakarta.websocket.Session;
import org.example.myapp.repository.UserBlockRepository;
import org.junit.jupiter.api.Test;

import java.util.Map;
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
        UserBlockRepository userBlockRepository = mock(UserBlockRepository.class);
        ChatBroadcaster broadcaster = new ChatBroadcaster();
        broadcaster.registry = registry;
        broadcaster.userBlockRepository = userBlockRepository;

        Session session = mock(Session.class);
        RemoteEndpoint.Async remote = mock(RemoteEndpoint.Async.class);
        when(registry.getSessions(1L)).thenReturn(Set.of(session));
        when(session.isOpen()).thenReturn(true);
        when(session.getAsyncRemote()).thenReturn(remote);

        broadcaster.broadcast(1L, "{\"event\":\"PRESENCE\"}");

        verify(remote).sendText(eq("{\"event\":\"PRESENCE\"}"), any(SendHandler.class));
        verify(session, never()).getBasicRemote();
    }

    @Test
    void broadcastPresenceSkipsRecipientsWhoBlockedTheUser() {
        ChatSessionRegistry registry = mock(ChatSessionRegistry.class);
        UserBlockRepository userBlockRepository = mock(UserBlockRepository.class);
        ChatBroadcaster broadcaster = new ChatBroadcaster();
        broadcaster.registry = registry;
        broadcaster.userBlockRepository = userBlockRepository;

        Session blockerSession = mock(Session.class);
        RemoteEndpoint.Async blockerRemote = mock(RemoteEndpoint.Async.class);
        Session otherSession = mock(Session.class);
        RemoteEndpoint.Async otherRemote = mock(RemoteEndpoint.Async.class);
        when(registry.getAllSessions()).thenReturn(Set.of(blockerSession, otherSession));
        when(blockerSession.getUserProperties()).thenReturn(Map.of("userId", 2L));
        when(otherSession.getUserProperties()).thenReturn(Map.of("userId", 3L));
        when(userBlockRepository.exists(2L, 1L)).thenReturn(true);
        when(userBlockRepository.exists(3L, 1L)).thenReturn(false);
        when(blockerSession.isOpen()).thenReturn(true);
        when(otherSession.isOpen()).thenReturn(true);
        when(otherSession.getAsyncRemote()).thenReturn(otherRemote);

        broadcaster.broadcastPresence(1L, true);

        verify(blockerSession, never()).getAsyncRemote();
        verify(otherRemote).sendText(eq("""
                {
                    "event": "PRESENCE",
                    "userId": 1,
                    "online": true
                }
                """), any(SendHandler.class));
    }
}
