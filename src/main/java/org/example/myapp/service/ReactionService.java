package org.example.myapp.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.example.myapp.dto.ReactionRequest;
import org.example.myapp.exception.DomainException;
import org.example.myapp.exception.EntityNotFoundException;
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

    @Transactional
    public void react(Long messageId, ReactionRequest req, Long userId) {

        Message message = messageRepository.findById(messageId);
        if (message == null) {
            throw new EntityNotFoundException("error.message.notfound");
        }

        User user = userRepository.findById(userId);
        if (user == null) {
            throw new EntityNotFoundException("error.user.notfound");
        }

        Long conversationId = message.getConversation().getId();

        // Check if user is participant
        boolean isParticipant = message.getConversation().getParticipants().stream()
                .anyMatch(cp -> cp.getUser().getId().equals(userId));

        if (!isParticipant) {
            throw DomainException.forbidden("error.message.notparticipant");
        }

        // Find existing reaction
        MessageReaction existing = reactionRepository.find(
                "message.id = ?1 AND user.id = ?2", messageId, userId
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
