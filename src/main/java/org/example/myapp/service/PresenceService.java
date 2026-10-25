package org.example.myapp.service;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class PresenceService {

    private final Map<Long, Integer> sessionCounts = new ConcurrentHashMap<>();

    public void addSession(Long userId) {
        sessionCounts.merge(userId, 1, Integer::sum);
    }

    public void removeSession(Long userId) {
        sessionCounts.merge(userId, -1, Integer::sum);
    }

    public boolean isOnline(Long userId) {
        return sessionCounts.getOrDefault(userId, 0) > 0;
    }

    public int getSessionCount(Long userId) {
        return sessionCounts.getOrDefault(userId, 0);
    }
}
