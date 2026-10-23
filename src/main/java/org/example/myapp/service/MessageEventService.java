package org.example.myapp.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import org.example.myapp.dto.MessageEventRequest;
import org.example.myapp.exception.DomainException;
import org.example.myapp.exception.EntityNotFoundException;
import org.example.myapp.model.*;
import org.example.myapp.repository.ConversationRepository;
import org.example.myapp.repository.MessageEventRepository;
import org.example.myapp.repository.MessageRepository;
import org.example.myapp.repository.UserRepository;
import org.example.myapp.ws.ChatBroadcaster;
import org.example.myapp.ws.DeliveredReceiptPayloadBuilder;
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

    @Transactional
    public MessageEvent createEvent(MessageEventRequest req, Long currentUserId) {

        // Validate conversation
        Conversation conversation = conversationRepository.findById(req.conversationId);
        if (conversation == null) {
            throw new EntityNotFoundException("error.conversation.notfound");
        }

        // Validate user
        User user = userRepository.findById(currentUserId);
        if (user == null) {
            throw new EntityNotFoundException("error.user.notfound");
        }

        // Check participant membership
        boolean isParticipant = conversation.getParticipants().stream()
                .anyMatch(cp -> cp.getUser().getId().equals(currentUserId));

        if (!isParticipant) {
            throw DomainException.forbidden("error.message.notparticipant");
        }

        Message message = null;

        // READ and DELIVERED require a message
        if (req.type == EventType.READ || req.type == EventType.DELIVERED) {

            message = messageRepository.findById(req.messageId);

            if (message == null) {
                throw new EntityNotFoundException("error.message.notfound");
            }

            if (!message.getConversation().getId().equals(req.conversationId)) {
                throw DomainException.badRequest("error.message.notinconversation");
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

            ConversationParticipant cp = conversation.getParticipants().stream()
                    .filter(p -> p.getUser().getId().equals(currentUserId))
                    .findFirst()
                    .orElseThrow(() -> new EntityNotFoundException("error.participant.notfound"));

            cp.setLastReadMessageId(req.messageId);
            cp.setUnreadCount(0);

        } else if (event.getType() == EventType.DELIVERED) {
            payload = DeliveredReceiptPayloadBuilder.build(event);
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
