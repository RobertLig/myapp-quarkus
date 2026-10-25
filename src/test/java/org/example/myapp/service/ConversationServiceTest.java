package org.example.myapp.service;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.example.myapp.model.Conversation;
import org.example.myapp.model.ConversationParticipant;
import org.example.myapp.model.User;
import org.example.myapp.repository.ConversationParticipantRepository;
import org.example.myapp.repository.ConversationRepository;
import org.example.myapp.repository.UserRepository;
import org.example.myapp.ws.ChatBroadcaster;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@QuarkusTest
public class ConversationServiceTest {

    @Inject
    ConversationService conversationService;

    @InjectMock
    ConversationRepository conversationRepository;

    @InjectMock
    ConversationParticipantRepository cpRepository;

    @InjectMock
    UserRepository userRepository;

    @InjectMock
    ChatBroadcaster broadcaster;

    private User user1;
    private Conversation conversation;
    private ConversationParticipant cp;

    @BeforeEach
    void setUp() {
        user1 = new User();
        user1.setId(1L);

        conversation = new Conversation();
        conversation.setId(10L);

        cp = new ConversationParticipant();
        cp.setUser(user1);
        cp.setConversation(conversation);
        cp.setUnreadCount(5);

        conversation.setParticipants(new ArrayList<>(List.of(cp)));
    }

    @Test
    void testDeleteConversationForUser_broadcastsUnreadCountReset() {
        when(conversationRepository.findById(10L)).thenReturn(conversation);
        when(userRepository.findById(1L)).thenReturn(user1);

        conversationService.deleteConversationForUser(10L, 1L);

        // Verify broadcastToUser was called with the user's ID
        verify(broadcaster).broadcastToUser(eq(1L), anyString());
    }

    @Test
    void testRestoreConversationForUser_broadcastsUnreadCountReset() {
        when(conversationRepository.findById(10L)).thenReturn(conversation);
        when(userRepository.findById(1L)).thenReturn(user1);

        conversationService.restoreConversationForUser(10L, 1L);

        // Verify broadcastToUser was called with the user's ID
        verify(broadcaster).broadcastToUser(eq(1L), anyString());
    }
}
