package org.example.myapp.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import org.example.myapp.dto.TypingEventRequest;
import org.example.myapp.exception.DomainException;
import org.example.myapp.exception.EntityNotFoundException;
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
}
