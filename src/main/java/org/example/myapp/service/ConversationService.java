package org.example.myapp.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import org.example.myapp.model.Conversation;
import org.example.myapp.model.ConversationParticipant;
import org.example.myapp.model.User;
import org.example.myapp.repository.ConversationRepository;
import org.example.myapp.repository.UserRepository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class ConversationService {

    @Inject
    ConversationRepository conversationRepository;

    @Inject
    UserRepository userRepository;

    @Inject
    UserBlockRepository userBlockRepository;

    public Conversation createConversation(List<Long> participantIds) {

        // Load users
        List<User> users = userRepository.findByIds(participantIds);

        if (users.size() != participantIds.size()) {
            throw new WebApplicationException("User not found", 404);
        }

        // Check blocks
        for (User u1 : users) {
            for (User u2 : users) {
                if (!u1.equals(u2) && userBlockRepository.exists(u1.id, u2.id)) {
                    throw new WebApplicationException("User is blocked", 403);
                }
            }
        }

        // Check if conversation already exists
        Optional<Conversation> existing = conversationRepository.findExistingConversation(participantIds);
        if (existing.isPresent()) {
            return existing.get();
        }

        // Create new conversation
        Conversation conversation = new Conversation();
        conversation.createdAt = new Date();
        conversationRepository.persist(conversation);

        // Add participants
        for (User user : users) {
            ConversationParticipant cp = new ConversationParticipant();
            cp.user = user;
            cp.conversation = conversation;
            cp.joinedAt = new Date();
            conversation.participants.add(cp);
        }

        return conversation;
    }
}
