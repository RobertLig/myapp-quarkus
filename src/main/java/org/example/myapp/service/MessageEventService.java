package org.example.myapp.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import org.example.myapp.dto.MessageEventRequest;
import org.example.myapp.model.*;
import org.example.myapp.repository.ConversationRepository;
import org.example.myapp.repository.MessageEventRepository;
import org.example.myapp.repository.MessageRepository;
import org.example.myapp.repository.UserRepository;
import org.example.myapp.ws.ChatBroadcaster;
import org.example.myapp.ws.ReadReceiptPayloadBuilder;

import java.util.Date;

@ApplicationScoped
public class MessageEventService {

    @Inject
    ConversationRepository conversationRepository;

    @Inject
    UserRepository userRepository;

    @Inject
    MessageRepository messageRepository;

    @Inject
    MessageEventRepository eventRepository;

    @Inject
    ChatBroadcaster broadcaster;

    public MessageEvent createEvent(MessageEventRequest req) {

        // Validate conversation
        Conversation conversation = conversationRepository.findById(req.conversationId);
        if (conversation == null) {
            throw new WebApplicationException("Conversation not found", 404);
        }

        // Validate user
        User user = userRepository.findById(req.userId);
        if (user == null) {
            throw new WebApplicationException("User not found", 404);
        }

        // Check participant membership
        boolean isParticipant = conversation.getParticipants().stream()
                .anyMatch(cp -> cp.getUser().getId().equals(req.userId));

        if (!isParticipant) {
            throw new WebApplicationException("User not in conversation", 403);
        }

        Message message = null;

        // READ and DELIVERED require a message
        if (req.type == EventType.READ || req.type == EventType.DELIVERED) {

            message = messageRepository.findById(req.messageId);

            if (message == null) {
                throw new WebApplicationException("Message not found", 404);
            }

            if (!message.getConversation().getId().equals(req.conversationId)) {
                throw new WebApplicationException("Message not in conversation", 400);
            }
        }

        // Create event
        MessageEvent event = new MessageEvent();
        event.setType(req.type);
        event.setUser(user);
        event.setMessage(message);
        event.setCreatedAt(new Date());

        eventRepository.persist(event);

        String payload;

        if (event.getType() == EventType.READ) {
            payload = ReadReceiptPayloadBuilder.build(event);
        } else {
            payload = """
        {
            "event": "%s",
            "userId": %d,
            "messageId": %s,
            "timestamp": %d
        }
        """.formatted(
                    event.getType(),
                    event.getUser().getId(),
                    event.getMessage() != null ? event.getMessage().getId() : "null",
                    event.getCreatedAt().getTime()
            );
        }

        broadcaster.broadcast(req.conversationId, payload);

        return event;
    }
}
