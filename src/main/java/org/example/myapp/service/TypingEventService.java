package org.example.myapp.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import org.example.myapp.dto.TypingEventRequest;
import org.example.myapp.exception.DomainException;
import org.example.myapp.exception.EntityNotFoundException;
import org.example.myapp.model.Conversation;
import org.example.myapp.model.EventType;
import org.example.myapp.model.User;
import org.example.myapp.repository.ConversationRepository;
import org.example.myapp.repository.UserBlockRepository;
import org.example.myapp.repository.UserRepository;
import org.example.myapp.ws.ChatBroadcaster;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class TypingEventService {

    private static final long DEBOUNCE_MS = 2000; // 2 seconds
    private final Map<String, Long> lastBroadcastTimes = new ConcurrentHashMap<>();

    @Inject
    ConversationRepository conversationRepository;

    @Inject
    UserRepository userRepository;

    @Inject
    UserBlockRepository userBlockRepository;

    @Inject
    ChatBroadcaster broadcaster;

    public void handleTypingEvent(TypingEventRequest req, Long currentUserId) {

        // Validate conversation
        Conversation conversation = conversationRepository.findById(req.conversationId);
        if (conversation == null) {
            throw new EntityNotFoundException("error.conversation.notfound");
        }

        // Validate user
        User user = userRepository.findById(currentUserId);
        if (user == null) {
            throw new EntityNotFoundException("error.user.notfound");
        }

        // Check participant membership
        boolean isParticipant = conversation.getParticipants().stream()
                .anyMatch(cp -> cp.getUser().getId().equals(currentUserId));

        if (!isParticipant) {
            throw DomainException.forbidden("error.message.notparticipant");
        }

        // Do not reveal typing activity to a participant who has blocked the sender.
        boolean senderIsBlocked = conversation.getParticipants().stream()
                .map(cp -> cp.getUser().getId())
                .filter(participantId -> !participantId.equals(currentUserId))
                .anyMatch(participantId -> userBlockRepository.exists(participantId, currentUserId));
        if (senderIsBlocked && req.type == EventType.TYPING_START) {
            return;
        }

        // Debounce repeated start events, but always deliver stop events so typing can resume.
        String key = currentUserId + ":" + req.conversationId;
        long now = System.currentTimeMillis();
        if (req.type == EventType.TYPING_START) {
            Long lastBroadcast = lastBroadcastTimes.get(key);
            if (lastBroadcast != null && (now - lastBroadcast) < DEBOUNCE_MS) {
                return;
            }
            lastBroadcastTimes.put(key, now);
        } else if (req.type == EventType.TYPING_STOP) {
            lastBroadcastTimes.remove(key);
        }

        // Build JSON payload
        String json = """
        {
            "event": "%s",
            "conversationId": %d,
            "userId": %d,
            "timestamp": %d
        }
        """.formatted(
                req.type,
                req.conversationId,
                currentUserId,
                System.currentTimeMillis()
        );

        // Broadcast to all participants
        broadcaster.broadcast(req.conversationId, json);
    }

    // Package-private method for testing - clears debounce state
    void clearDebounceState() {
        lastBroadcastTimes.clear();
    }
}
