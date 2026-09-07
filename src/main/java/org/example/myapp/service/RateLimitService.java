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

    private final Map<String, Counter> registerByIp = new ConcurrentHashMap<>();
    private final Map<String, Counter> resetByIp = new ConcurrentHashMap<>();

    private static final int REGISTER_LIMIT = 5;      // 5 per hour per IP
    private static final int RESET_LIMIT = 5;         // 5 per hour per IP
    private static final long WINDOW_SECONDS = 3600;  // 1 hour

    private final Map<String, Integer> ipSuspicion = new ConcurrentHashMap<>();

    private static final int SUSPICION_THRESHOLD = 10; // block after 10 suspicious events

    private boolean allowed(Map<String, Counter> store, String key, int limit) {
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

    public boolean allowRegister(String ip) {
        return allowed(registerByIp, ip, REGISTER_LIMIT);
    }

    public boolean allowReset(String ip) {
        return allowed(resetByIp, ip, RESET_LIMIT);
    }

    public void addSuspicion(String ip, int amount) {
        ipSuspicion.merge(ip, amount, Integer::sum);
    }

    public boolean isThrottled(String ip) {
        return ipSuspicion.getOrDefault(ip, 0) >= SUSPICION_THRESHOLD;
    }

    public void decaySuspicion(String ip) {
        ipSuspicion.computeIfPresent(ip, (k, v) -> Math.max(0, v - 1));
    }
}
