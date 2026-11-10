package org.example.myapp;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.example.myapp.mapper.MessageMapper;
import org.example.myapp.model.*;
import org.example.myapp.repository.*;
import org.example.myapp.service.ImageStoreService;
import org.example.myapp.service.UserService;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

@QuarkusTest
class UserDeletionWithMessagesTest {

    @Inject
    EntityManager entityManager;

    @Inject
    UserService userService;

    @Inject
    UserRepository userRepository;

    @Inject
    AnnouncementRepository announcementRepository;

    @Inject
    ConversationRepository conversationRepository;

    @Inject
    ConversationParticipantRepository participantRepository;

    @Inject
    MessageRepository messageRepository;

    @Inject
    MessageAttachmentRepository attachmentRepository;

    @Inject
    MessageReactionRepository reactionRepository;

    @Inject
    MessageEventRepository eventRepository;

    @InjectMock
    ImageStoreService imageStoreService;

    @Test
    @Transactional
    void deletingUserKeepsConversationHistoryAndRemovesAccountReferences() {
        entityManager.createNativeQuery(
                "SELECT setval(pg_get_serial_sequence('users', 'id'), COALESCE((SELECT MAX(id) FROM users), 1), EXISTS (SELECT 1 FROM users))")
                .getSingleResult();

        User accountToDelete = new User();
        accountToDelete.setEmail("delete-with-messages@example.com");
        accountToDelete.setName("Deleted user");
        accountToDelete.setPhotoUrl("https://example.com/avatar.png");
        userRepository.persistAndFlush(accountToDelete);

        User otherParticipant = new User();
        otherParticipant.setEmail("conversation-participant@example.com");
        otherParticipant.setName("Other participant");
        userRepository.persistAndFlush(otherParticipant);

        Announcement announcement = new Announcement("sender", accountToDelete);
        announcementRepository.persistAndFlush(announcement);

        Conversation conversation = new Conversation();
        conversation.setAnnouncement(announcement);
        conversation.setCreatedAt(new Date());
        conversation.setType(ConversationType.USER_TO_USER);
        conversationRepository.persistAndFlush(conversation);

        ConversationParticipant deletedParticipant = participant(conversation, accountToDelete);
        ConversationParticipant remainingParticipant = participant(conversation, otherParticipant);
        participantRepository.persistAndFlush(deletedParticipant);
        participantRepository.persistAndFlush(remainingParticipant);

        Message message = new Message();
        message.setConversation(conversation);
        message.setSender(accountToDelete);
        message.setContent("Conversation history");
        message.setCreatedAt(new Date());
        messageRepository.persistAndFlush(message);

        MessageAttachment attachment = new MessageAttachment();
        attachment.setMessage(message);
        attachment.setUser(accountToDelete);
        attachment.setUrl("https://example.com/message-attachment.png");
        attachment.setType("image/png");
        attachment.setSize(128L);
        attachmentRepository.persistAndFlush(attachment);

        MessageReaction reaction = new MessageReaction();
        reaction.setMessage(message);
        reaction.setUser(accountToDelete);
        reaction.setReaction("like");
        reactionRepository.persistAndFlush(reaction);

        MessageEvent event = new MessageEvent();
        event.setMessage(message);
        event.setUser(accountToDelete);
        event.setType(EventType.DELIVERED);
        event.setCreatedAt(new Date());
        eventRepository.persistAndFlush(event);

        Long userId = accountToDelete.getId();
        Long announcementId = announcement.getId();
        Long conversationId = conversation.getId();
        Long messageId = message.getId();

        userService.deleteUser(userId);
        entityManager.flush();
        entityManager.clear();

        assertFalse(userRepository.findByIdOptional(userId).isPresent());
        assertFalse(announcementRepository.findByIdOptional(announcementId).isPresent());
        assertEquals(1, conversationRepository.count("id = ?1 and announcement is null", conversationId));
        assertEquals(1, participantRepository.find("conversation.id", conversationId).count());
        assertNull(messageRepository.findById(messageId).getSender());
        assertNull(MessageMapper.toDTO(messageRepository.findById(messageId)).senderId);
        assertEquals(0, attachmentRepository.count("user.id", userId));
        assertEquals(0, reactionRepository.count("user.id", userId));
        assertEquals(0, eventRepository.count("user.id", userId));
        verify(imageStoreService).deleteAll(List.of(
                "https://example.com/message-attachment.png",
                "https://example.com/avatar.png"
        ));
    }

    private ConversationParticipant participant(Conversation conversation, User user) {
        ConversationParticipant participant = new ConversationParticipant();
        participant.setConversation(conversation);
        participant.setUser(user);
        participant.setJoinedAt(new Date());
        return participant;
    }
}
