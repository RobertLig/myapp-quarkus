package org.example.myapp.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import org.example.myapp.model.Conversation;
import org.example.myapp.model.ConversationParticipant;
import org.example.myapp.model.ConversationType;
import org.example.myapp.model.User;
import org.example.myapp.repository.ConversationParticipantRepository;
import org.example.myapp.repository.ConversationRepository;
import org.example.myapp.repository.UserBlockRepository;
import org.example.myapp.repository.UserRepository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class ConversationService {

    @Inject
    ConversationRepository conversationRepository;

    @Inject
    ConversationParticipantRepository cpRepository;

    @Inject
    UserRepository userRepository;

    @Inject
    UserBlockRepository userBlockRepository;

    public Conversation createConversation(List<Long> participantIds) {

        // Load users
        List<User> users = userRepository.findByIds(participantIds);

        if (users.size() != participantIds.size()) {
            throw new WebApplicationException("error.user.notfound", 404);
        }

        // Check blocks
        for (User u1 : users) {
            for (User u2 : users) {
                if (!u1.equals(u2) && userBlockRepository.exists(u1.getId(), u2.getId())) {
                    throw new WebApplicationException("error.user.blocked", 403);
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
        conversation.setCreatedAt(new Date());
        conversationRepository.persist(conversation);

        // Add participants
        for (User user : users) {
            ConversationParticipant cp = new ConversationParticipant();
            cp.setUser(user);
            cp.setConversation(conversation);
            cp.setJoinedAt(new Date());
            conversation.getParticipants().add(cp);
        }

        return conversation;
    }

    public void deleteConversationForUser(Long conversationId, Long userId) {

        // Load conversation
        Conversation conversation = conversationRepository.findById(conversationId);
        if (conversation == null) {
            throw new WebApplicationException("error.conversation.notfound", 404);
        }

        // Load user
        User user = userRepository.findById(userId);
        if (user == null) {
            throw new WebApplicationException("error.user.notfound", 404);
        }

        // Find participant
        ConversationParticipant cp = conversation.getParticipants().stream()
                .filter(p -> p.getUser().getId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new WebApplicationException("error.message.notparticipant", 403));

        // Mark as deleted
        cp.setDeletedAt(new Date());

        // Reset unread counter
        cp.setUnreadCount(0);
    }

    public void restoreConversationForUser(Long conversationId, Long userId) {

        // Load conversation
        Conversation conversation = conversationRepository.findById(conversationId);
        if (conversation == null) {
            throw new WebApplicationException("error.conversation.notfound", 404);
        }

        // Load user
        User user = userRepository.findById(userId);
        if (user == null) {
            throw new WebApplicationException("error.user.notfound", 404);
        }

        // Find participant
        ConversationParticipant cp = conversation.getParticipants().stream()
                .filter(p -> p.getUser().getId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new WebApplicationException("error.message.notparticipant", 403));

        // Restore conversation
        cp.setDeletedAt(null);

        // Reset unread counter (recommended)
        cp.setUnreadCount(0);
    }

    public List<ConversationParticipant> listConversations(Long userId) {

        // Validate user
        User user = userRepository.findById(userId);
        if (user == null) {
            throw new WebApplicationException("error.user.notfound", 404);
        }

        // Load all conversations for this user
        List<ConversationParticipant> cps = cpRepository.findByUser(userId);

        // Filter out conversations deleted by this user
        return cps.stream()
                .filter(cp -> cp.getDeletedAt() == null)
                .toList();
    }

    public Conversation getOrCreateSupportConversation(Long userId, Long adminId) {

        // 1. Load user
        User user = userRepository.findById(userId);
        if (user == null) {
            throw new WebApplicationException("error.user.notfound", 404);
        }

        // 2. Load admin
        User admin = userRepository.findById(adminId);
        if (admin == null) {
            throw new WebApplicationException("error.admin.notfound", 404);
        }

        // 3. Check if support conversation already exists
        Conversation existing = conversationRepository.find(
                "type = ?1 AND EXISTS (" +
                        "SELECT cp FROM ConversationParticipant cp " +
                        "WHERE cp.conversation.id = Conversation.id AND cp.user.id = ?2" +
                        ")",
                ConversationType.USER_TO_ADMIN,
                userId
        ).firstResult();

        if (existing != null) {
            return existing;
        }

        // 4. Create new support conversation
        Conversation conversation = new Conversation();
        conversation.setCreatedAt(new Date());
        conversation.setType(ConversationType.USER_TO_ADMIN);
        conversationRepository.persist(conversation);

        // 5. Add user participant
        ConversationParticipant pUser = new ConversationParticipant();
        pUser.setUser(user);
        pUser.setConversation(conversation);
        pUser.setJoinedAt(new Date());
        cpRepository.persist(pUser);

        // 6. Add admin participant
        ConversationParticipant pAdmin = new ConversationParticipant();
        pAdmin.setUser(admin);
        pAdmin.setConversation(conversation);
        pAdmin.setJoinedAt(new Date());
        cpRepository.persist(pAdmin);

        // 7. Return the new conversation
        return conversation;
    }

    public List<ConversationParticipant> listSupportConversations(Long adminId) {

        // Validate admin
        User admin = userRepository.findById(adminId);
        if (admin == null) {
            throw new WebApplicationException("error.admin.notfound", 404);
        }

        // Load all conversations where admin is a participant
        List<ConversationParticipant> cps = cpRepository.findByUser(adminId);

        // Filter only USER_TO_ADMIN conversations
        return cps.stream()
                .filter(cp -> cp.getDeletedAt() == null)
                .filter(cp -> cp.getConversation().getType() == ConversationType.USER_TO_ADMIN)
                .toList();
    }
}
