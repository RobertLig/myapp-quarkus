package org.example.myapp.service;

import jakarta.enterprise.context.ApplicationScoped;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class RateLimitService {

    private static class Counter {
        int count;
        Instant windowStart;
    }

    private static final long WINDOW_SECONDS = 3600;  // 1 hour

    private final Map<Object, Counter> messageByUser = new ConcurrentHashMap<>();

    private static final int MESSAGE_LIMIT = 30; // 30 messages per hour per user

    private boolean allowed(Map<Object, Counter> store, Object key, int limit)
    {
        Instant now = Instant.now();
        Counter c = store.computeIfAbsent(key, k -> {
            Counter nc = new Counter();
            nc.count = 0;
            nc.windowStart = now;
            return nc;
        });

        if (now.isAfter(c.windowStart.plusSeconds(WINDOW_SECONDS))) {
            c.windowStart = now;
            c.count = 0;
        }

        c.count++;
        return c.count <= limit;
    }

    public boolean allowMessage(Long userId) {

        return allowed(messageByUser, userId, MESSAGE_LIMIT);
    }
}
