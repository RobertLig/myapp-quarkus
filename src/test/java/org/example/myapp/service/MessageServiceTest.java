package org.example.myapp.service;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.example.myapp.dto.MessagePageDTO;
import org.example.myapp.exception.DomainException;
import org.example.myapp.exception.EntityNotFoundException;
import org.example.myapp.model.Conversation;
import org.example.myapp.model.ConversationParticipant;
import org.example.myapp.model.Message;
import org.example.myapp.model.User;
import org.example.myapp.repository.ConversationRepository;
import org.example.myapp.repository.MessageEventRepository;
import org.example.myapp.repository.MessageRepository;
import org.example.myapp.repository.UserBlockRepository;
import org.example.myapp.repository.UserRepository;
import org.example.myapp.ws.ChatBroadcaster;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@QuarkusTest
public class MessageServiceTest {

    @Inject
    MessageService messageService;

    @InjectMock
    ConversationRepository conversationRepository;

    @InjectMock
    MessageRepository messageRepository;

    @InjectMock
    UserRepository userRepository;

    @InjectMock
    UserBlockRepository userBlockRepository;

    @InjectMock
    ChatBroadcaster broadcaster;

    @InjectMock
    MessageEventRepository eventRepository;

    @InjectMock
    RateLimitService rateLimitService;

    private User user1;
    private User user2;
    private Conversation conversation;

    @BeforeEach
    void setUp() {
        user1 = new User();
        user1.setId(1L);

        user2 = new User();
        user2.setId(2L);

        conversation = new Conversation();
        conversation.setId(10L);

        ConversationParticipant cp1 = new ConversationParticipant();
        cp1.setUser(user1);
        cp1.setConversation(conversation);

        ConversationParticipant cp2 = new ConversationParticipant();
        cp2.setUser(user2);
        cp2.setConversation(conversation);

        conversation.setParticipants(new ArrayList<>(List.of(cp1, cp2)));
    }

    @Test
    void testSearch_queryNullOrBlank_throwsBadRequest() {
        DomainException exNull = assertThrows(DomainException.class, () ->
                messageService.search(10L, 1L, null, null, 30));
        assertEquals(400, exNull.getStatus());
        assertEquals("query.required", exNull.getMessageKey());

        DomainException exBlank = assertThrows(DomainException.class, () ->
                messageService.search(10L, 1L, "   ", null, 30));
        assertEquals(400, exBlank.getStatus());
        assertEquals("query.required", exBlank.getMessageKey());
    }

    @Test
    void testSearch_conversationNotFound_throwsEntityNotFoundException() {
        when(conversationRepository.findById(999L)).thenReturn(null);

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class, () ->
                messageService.search(999L, 1L, "hello", null, 30));
        assertEquals("error.conversation.notfound", ex.getMessageKey());
    }

    @Test
    void testSearch_userNotFound_throwsEntityNotFoundException() {
        when(conversationRepository.findById(10L)).thenReturn(conversation);
        when(userRepository.findById(999L)).thenReturn(null);

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class, () ->
                messageService.search(10L, 999L, "hello", null, 30));
        assertEquals("error.user.notfound", ex.getMessageKey());
    }

    @Test
    void testSearch_userNotParticipant_throwsForbidden() {
        User nonParticipant = new User();
        nonParticipant.setId(3L);

        when(conversationRepository.findById(10L)).thenReturn(conversation);
        when(userRepository.findById(3L)).thenReturn(nonParticipant);

        DomainException ex = assertThrows(DomainException.class, () ->
                messageService.search(10L, 3L, "hello", null, 30));
        assertEquals(403, ex.getStatus());
        assertEquals("error.message.notparticipant", ex.getMessageKey());
    }

    @Test
    void testSearch_success_filtersRecipientDeletedMessagesAndReturnsPage() {
        when(conversationRepository.findById(10L)).thenReturn(conversation);
        when(userRepository.findById(1L)).thenReturn(user1);

        // Message 1: sender = user2, deletedForRecipientAt = now -> should be filtered out for user1
        Message m1 = new Message();
        m1.setConversation(conversation);
        m1.setSender(user2);
        m1.setContent("hello secret");
        m1.setCreatedAt(new Date(1000L));
        m1.setDeletedForRecipientAt(new Date(1500L));

        // Message 2: sender = user1 (current user), deletedForRecipientAt = now -> should NOT be filtered out
        Message m2 = new Message();
        m2.setConversation(conversation);
        m2.setSender(user1);
        m2.setContent("hello from user1");
        m2.setCreatedAt(new Date(2000L));
        m2.setDeletedForRecipientAt(new Date(2500L));

        // Message 3: sender = user2, not deleted -> should NOT be filtered out
        Message m3 = new Message();
        m3.setConversation(conversation);
        m3.setSender(user2);
        m3.setContent("hello world");
        m3.setCreatedAt(new Date(3000L));

        when(messageRepository.search(eq(10L), eq("hello"), isNull(), eq(30)))
                .thenReturn(List.of(m3, m2, m1));

        when(messageRepository.count(anyString(), any(Object[].class)))
                .thenReturn(1L);

        MessagePageDTO result = messageService.search(10L, 1L, "hello", null, 30);

        assertNotNull(result);
        assertEquals(2, result.messages.size());
        assertEquals("hello world", result.messages.get(0).content);
        assertEquals("hello from user1", result.messages.get(1).content);
        assertTrue(result.hasMore);
    }
}
