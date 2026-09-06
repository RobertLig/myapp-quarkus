package org.example.myapp.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import org.example.myapp.dto.TypingEventRequest;
import org.example.myapp.model.Conversation;
import org.example.myapp.model.User;
import org.example.myapp.repository.ConversationRepository;
import org.example.myapp.repository.UserRepository;
import org.example.myapp.ws.ChatBroadcaster;

@ApplicationScoped
public class TypingEventService {

    @Inject
    ConversationRepository conversationRepository;

    @Inject
    UserRepository userRepository;

    @Inject
    ChatBroadcaster broadcaster;

    public void handleTypingEvent(TypingEventRequest req) {

        // Validate conversation
        Conversation conversation = conversationRepository.findById(req.conversationId);
        if (conversation == null) {
            throw new WebApplicationException("error.conversation.notfound", 404);
        }

        // Validate user
        User user = userRepository.findById(req.userId);
        if (user == null) {
            throw new WebApplicationException("error.user.notfound", 404);
        }

        // Check participant membership
        boolean isParticipant = conversation.getParticipants().stream()
                .anyMatch(cp -> cp.getUser().getId().equals(req.userId));

        if (!isParticipant) {
            throw new WebApplicationException("error.message.notparticipant", 403);
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
                req.userId,
                System.currentTimeMillis()
        );

        // Broadcast to all participants
        broadcaster.broadcast(req.conversationId, json);
    }
}
