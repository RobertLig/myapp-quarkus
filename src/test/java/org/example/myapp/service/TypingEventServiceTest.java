package org.example.myapp.service;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.example.myapp.dto.TypingEventRequest;
import org.example.myapp.model.Conversation;
import org.example.myapp.model.ConversationParticipant;
import org.example.myapp.model.EventType;
import org.example.myapp.model.User;
import org.example.myapp.repository.ConversationRepository;
import org.example.myapp.repository.UserRepository;
import org.example.myapp.ws.ChatBroadcaster;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@QuarkusTest
public class TypingEventServiceTest {

    @Inject
    TypingEventService typingEventService;

    @InjectMock
    ConversationRepository conversationRepository;

    @InjectMock
    UserRepository userRepository;

    @InjectMock
    ChatBroadcaster broadcaster;

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

    @AfterEach
    void tearDown() {
        // Clear debounce state between tests
        typingEventService.clearDebounceState();
    }

    @Test
    void testHandleTypingEvent_broadcastsTypingEvent() {
        TypingEventRequest req = new TypingEventRequest();
        req.conversationId = 10L;
        req.type = EventType.TYPING_START;

        when(conversationRepository.findById(10L)).thenReturn(conversation);
        when(userRepository.findById(1L)).thenReturn(user1);

        typingEventService.handleTypingEvent(req, 1L);

        // Verify broadcast was called for the typing event
        verify(broadcaster).broadcast(eq(10L), anyString());
    }

    @Test
    void testHandleTypingEvent_debouncePreventsBroadcastWithinCooldown() {
        TypingEventRequest req = new TypingEventRequest();
        req.conversationId = 10L;
        req.type = EventType.TYPING_START;

        when(conversationRepository.findById(10L)).thenReturn(conversation);
        when(userRepository.findById(1L)).thenReturn(user1);

        // First call should broadcast
        typingEventService.handleTypingEvent(req, 1L);

        // Second immediate call should be debounced (no additional broadcast)
        typingEventService.handleTypingEvent(req, 1L);

        // Verify broadcast was called exactly once (debounce prevented second call)
        verify(broadcaster, times(1)).broadcast(eq(10L), anyString());
    }
}
