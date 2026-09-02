package org.example.myapp.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import org.example.myapp.dto.ReactionRequest;
import org.example.myapp.model.*;
import org.example.myapp.repository.MessageEventRepository;
import org.example.myapp.repository.MessageReactionRepository;
import org.example.myapp.repository.MessageRepository;
import org.example.myapp.repository.UserRepository;
import org.example.myapp.ws.ChatBroadcaster;
import org.example.myapp.ws.ReactionPayloadBuilder;

import java.util.Date;

@ApplicationScoped
public class ReactionService {

    @Inject
    MessageRepository messageRepository;

    @Inject
    UserRepository userRepository;

    @Inject
    MessageReactionRepository reactionRepository;

    @Inject
    MessageEventRepository eventRepository;

    @Inject
    ChatBroadcaster broadcaster;

    public void react(Long messageId, ReactionRequest req) {

        Message message = messageRepository.findById(messageId);
        if (message == null) {
            throw new WebApplicationException("Message not found", 404);
        }

        User user = userRepository.findById(req.userId);
        if (user == null) {
            throw new WebApplicationException("User not found", 404);
        }

        Long conversationId = message.getConversation().getId();

        // Check if user is participant
        boolean isParticipant = message.getConversation().getParticipants().stream()
                .anyMatch(cp -> cp.getUser().getId().equals(req.userId));

        if (!isParticipant) {
            throw new WebApplicationException("User is not a participant", 403);
        }

        // Find existing reaction
        MessageReaction existing = reactionRepository.find(
                "message.id = ?1 AND user.id = ?2", messageId, req.userId
        ).firstResult();

        // Remove reaction
        if (req.reaction == null || req.reaction.isBlank()) {
            if (existing != null) {
                reactionRepository.delete(existing);
                broadcastReaction(message, user, null);
            }
            return;
        }

        // Update existing reaction
        if (existing != null) {
            existing.setReaction(req.reaction);
            broadcastReaction(message, user, req.reaction);
            return;
        }

        // Create new reaction
        MessageReaction reaction = new MessageReaction();
        reaction.setMessage(message);
        reaction.setUser(user);
        reaction.setReaction(req.reaction);

        reactionRepository.persist(reaction);

        broadcastReaction(message, user, req.reaction);
    }

    private void broadcastReaction(Message message, User user, String reaction) {

        // Create event
        MessageEvent event = new MessageEvent();
        event.setType(EventType.REACTION);
        event.setMessage(message);
        event.setUser(user);
        event.setCreatedAt(new Date());

        eventRepository.persist(event);

        // Build JSON
        String json = ReactionPayloadBuilder.build(message, user, reaction);

        // Broadcast
        broadcaster.broadcast(message.getConversation().getId(), json);
    }
}
