package org.example.myapp.service;

import io.smallrye.common.annotation.Blocking;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import org.example.myapp.exception.DomainException;
import org.example.myapp.exception.EntityNotFoundException;
import org.example.myapp.dto.ConversationListItemDTO;
import org.example.myapp.model.*;
import org.example.myapp.mapper.ConversationMapper;
import org.example.myapp.repository.ConversationParticipantRepository;
import org.example.myapp.repository.ConversationRepository;
import org.example.myapp.repository.UserBlockRepository;
import org.example.myapp.repository.UserRepository;
import org.example.myapp.repository.AnnouncementRepository;
import org.hibernate.Hibernate;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@ApplicationScoped
public class ConversationService {

    @Inject
    ConversationRepository conversationRepository;

    @Inject
    ConversationParticipantRepository cpRepository;

    @Inject
    UserRepository userRepository;

    @Inject
    AnnouncementRepository announcementRepository;

    @Inject
    UserBlockRepository userBlockRepository;

    @Inject
    ConversationMapper conversationMapper;

    @Transactional
    public Conversation createConversation(List<Long> participantIds, Long announcementId, Long currentUserId) {

        if (participantIds == null || participantIds.isEmpty()) {
            throw DomainException.badRequest("error.participantIDs.required");
        }

        // Remove duplicate participant IDs and collect into a MUTABLE list
        List<Long> uniqueParticipantIds = participantIds.stream()
                .distinct()
                .collect(Collectors.toCollection(ArrayList::new));

        // Ensure the logged-in user is included in the participant list first
        if (!uniqueParticipantIds.contains(currentUserId)) {
            uniqueParticipantIds.add(currentUserId);
        }

        // Now validate the size (e.g. at least 2 distinct participants after the logged-in user is added)
        if (uniqueParticipantIds.size() < 2) {
            throw DomainException.badRequest("error.conversation.invalid_participants");
        }

        // Load users using the unique list
        List<User> users = userRepository.findByIds(uniqueParticipantIds);

        if (users.size() != uniqueParticipantIds.size()) {
            throw new EntityNotFoundException("error.user.notfound");
        }

        // Check blocks
        for (User u1 : users) {
            for (User u2 : users) {
                if (!u1.equals(u2) && userBlockRepository.exists(u1.getId(), u2.getId())) {
                    throw DomainException.forbidden("error.user.blocked");
                }
            }
        }

        // Check if conversation already exists
        Optional<Conversation> existing = conversationRepository.findExistingConversation(uniqueParticipantIds);
        if (existing.isPresent()) {
            Conversation conv = existing.get();
            Hibernate.initialize(conv.getParticipants()); // Force load participants while session is open!
            return conv;
        }

        Announcement announcement = null;
        if (announcementId != null) {
            announcement = announcementRepository.findById(announcementId);
            if (announcement == null) {
                throw new EntityNotFoundException("error.announcement.notfound");
            }
        }

        // Determine conversation type based on participants' roles
        ConversationType determinedType = ConversationType.USER_TO_USER;
        for (User user : users) {
            if (user.getRole() == Role.ADMIN) {
                determinedType = ConversationType.USER_TO_ADMIN;
                break; // Stop checking once we find an admin
            }
        }

        // Create new conversation
        Conversation conversation = new Conversation();
        conversation.setAnnouncement(announcement);
        conversation.setCreatedAt(new Date());
        conversation.setType(determinedType);
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

    @Transactional(Transactional.TxType.SUPPORTS)
    public List<ConversationListItemDTO> listConversations(Long userId) {
        // Optional: validate user exists if necessary
        User user = userRepository.findById(userId);
        if (user == null) {
            throw new EntityNotFoundException("error.user.notfound");
        }

        // Fetch pre-loaded conversations and participants in one go
        List<ConversationParticipant> cps = cpRepository.findActiveByUserWithDetails(userId);

        // Map directly to DTOs safely within the open transaction
        return cps.stream()
                .map(conversationMapper::toListItemDTO)
                .toList();
    }

    @Transactional // This manages the transaction and request context automatically when called from a worker thread
    public boolean isUserInConversation(Long userId, Long conversationId) {
        return cpRepository.isUserInConversation(userId, conversationId);
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

    @Transactional(Transactional.TxType.SUPPORTS)
    public List<ConversationListItemDTO> listSupportConversations(Long userId) {
        // Validate user exists
        User user = userRepository.findById(userId);
        if (user == null) {
            throw new EntityNotFoundException("error.user.notfound");
        }

        // Fetch pre-loaded active conversations and participants
        List<ConversationParticipant> cps = cpRepository.findActiveByUserWithDetails(userId);

        // Filter only USER_TO_ADMIN conversations and map directly to DTOs
        return cps.stream()
                .filter(cp -> cp.getConversation().getType() == ConversationType.USER_TO_ADMIN)
                .map(conversationMapper::toListItemDTO)
                .toList();
    }
}
