package org.example.myapp.service;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.example.myapp.dto.AttachmentRequest;
import org.example.myapp.model.Conversation;
import org.example.myapp.model.ConversationParticipant;
import org.example.myapp.model.Message;
import org.example.myapp.model.MessageAttachment;
import org.example.myapp.model.User;
import org.example.myapp.repository.MessageAttachmentRepository;
import org.example.myapp.repository.MessageEventRepository;
import org.example.myapp.repository.MessageRepository;
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
public class AttachmentServiceTest {

    @Inject
    AttachmentService attachmentService;

    @InjectMock
    MessageRepository messageRepository;

    @InjectMock
    UserRepository userRepository;

    @InjectMock
    MessageAttachmentRepository attachmentRepository;

    @InjectMock
    MessageEventRepository eventRepository;

    @InjectMock
    ChatBroadcaster broadcaster;

    @InjectMock
    ImageLimitService imageLimitService;

    private User user1;
    private User user2;
    private Conversation conversation;
    private Message message;

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

        message = new Message();
        message.setConversation(conversation);
        message.setSender(user1);
        message.setContent("Test message");
        message.setCreatedAt(new Date());
    }

    @Test
    void testAddAttachment_broadcastsAttachmentEvent() {
        AttachmentRequest req = new AttachmentRequest();
        req.url = "http://example.com/file.jpg";
        req.type = "image";
        req.size = 1024L;

        when(messageRepository.findById(100L)).thenReturn(message);
        when(userRepository.findById(1L)).thenReturn(user1);
        when(imageLimitService.canAddMessageAttachment(message)).thenReturn(true);

        attachmentService.addAttachment(100L, req, 1L);

        // Verify broadcast was called for the attachment event
        verify(broadcaster).broadcast(eq(10L), anyString());
    }

    @Test
    void testAddAttachment_broadcastsUnreadCountToRecipients() {
        AttachmentRequest req = new AttachmentRequest();
        req.url = "http://example.com/file.jpg";
        req.type = "image";
        req.size = 1024L;

        when(messageRepository.findById(100L)).thenReturn(message);
        when(userRepository.findById(1L)).thenReturn(user1);
        when(imageLimitService.canAddMessageAttachment(message)).thenReturn(true);

        attachmentService.addAttachment(100L, req, 1L);

        // Verify broadcastToUser was called for recipient (user2, not sender user1)
        verify(broadcaster).broadcastToUser(eq(2L), anyString());
    }
}
