package org.example.myapp.ws;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.Session;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class ChatSessionRegistry {

    private final Map<Long, Set<Session>> sessions = new ConcurrentHashMap<>();
    private final Map<Long, Set<Session>> sessionsByUserId = new ConcurrentHashMap<>();

    public void addSession(Long conversationId, Long userId, Session session) {
        sessions
                .computeIfAbsent(conversationId, id -> ConcurrentHashMap.newKeySet())
                .add(session);

        sessionsByUserId
                .computeIfAbsent(userId, id -> ConcurrentHashMap.newKeySet())
                .add(session);
    }

    public void removeSession(Long conversationId, Long userId, Session session) {
        Set<Session> set = sessions.get(conversationId);
        if (set != null) {
            set.remove(session);
        }

        Set<Session> userSet = sessionsByUserId.get(userId);
        if (userSet != null) {
            userSet.remove(session);
        }
    }

    public Set<Session> getSessions(Long conversationId) {
        return sessions.getOrDefault(conversationId, Set.of());
    }

    public Set<Session> getSessionsByUserId(Long userId) {
        return sessionsByUserId.getOrDefault(userId, Set.of());
    }

    public Set<Session> getAllSessions() {
        Set<Session> all = ConcurrentHashMap.newKeySet();

        for (Set<Session> set : sessions.values()) {
            all.addAll(set);
        }

        return all;
    }

}
