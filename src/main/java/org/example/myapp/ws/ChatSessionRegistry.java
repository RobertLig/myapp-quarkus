package org.example.myapp.ws;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.Session;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class ChatSessionRegistry {

    private final Map<Long, Set<Session>> sessions = new ConcurrentHashMap<>();

    public void addSession(Long conversationId, Session session) {
        sessions
                .computeIfAbsent(conversationId, id -> ConcurrentHashMap.newKeySet())
                .add(session);
    }

    public void removeSession(Long conversationId, Session session) {
        Set<Session> set = sessions.get(conversationId);
        if (set != null) {
            set.remove(session);
        }
    }

    public Set<Session> getSessions(Long conversationId) {

        return sessions.getOrDefault(conversationId, Set.of());
    }

    public Set<Session> getAllSessions() {
        Set<Session> all = ConcurrentHashMap.newKeySet();

        for (Set<Session> set : sessions.values()) {
            all.addAll(set);
        }

        return all;
    }

}
