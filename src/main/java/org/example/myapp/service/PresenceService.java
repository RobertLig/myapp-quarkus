package org.example.myapp.service;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class PresenceService {

    private final Map<Long, Boolean> online = new ConcurrentHashMap<>();

    public void setOnline(Long userId) {
        online.put(userId, true);
    }

    public void setOffline(Long userId) {
        online.put(userId, false);
    }

    public boolean isOnline(Long userId) {
        return online.getOrDefault(userId, false);
    }
}
