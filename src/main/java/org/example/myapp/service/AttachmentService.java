package org.example.myapp.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import org.example.myapp.dto.AttachmentRequest;
import org.example.myapp.model.*;
import org.example.myapp.repository.*;
import org.example.myapp.ws.ChatBroadcaster;

import java.util.Date;

@ApplicationScoped
public class AttachmentService {

    @Inject
    MessageRepository messageRepository;

    @Inject
    UserRepository userRepository;

    @Inject
    MessageAttachmentRepository attachmentRepository;

    @Inject
    MessageEventRepository eventRepository;

    @Inject
    ChatBroadcaster broadcaster;

    public void addAttachment(Long messageId, AttachmentRequest req) {

        Message message = messageRepository.findById(messageId);
        if (message == null) {
            throw new WebApplicationException("Message not found", 404);
        }

        User user = userRepository.findById(req.userId);
        if (user == null) {
            throw new WebApplicationException("error.user.notfound", 404);
        }

        // Validate user is a participant
        boolean isParticipant = message.getConversation().getParticipants().stream()
                .anyMatch(cp -> cp.getUser().getId().equals(req.userId));

        if (!isParticipant) {
            throw new WebApplicationException("error.message.notparticipant", 403);
        }

        // Create attachment
        MessageAttachment attachment = new MessageAttachment();
        attachment.setMessage(message);
        attachment.setUser(user);
        attachment.setUrl(req.url);
        attachment.setType(req.type);
        attachment.setSize(req.size);
        attachment.setCreatedAt(new Date());

        attachmentRepository.persist(attachment);

        broadcastAttachment(message, user, attachment);
    }

    private void broadcastAttachment(Message message, User user, MessageAttachment attachment) {

        // Create event
        MessageEvent event = new MessageEvent();
        event.setType(EventType.ATTACHMENT);
        event.setMessage(message);
        event.setUser(user);
        event.setCreatedAt(new Date());

        eventRepository.persist(event);

        // Build JSON payload
        String json = """
        {
            "event": "ATTACHMENT",
            "conversationId": %d,
            "messageId": %d,
            "userId": %d,
            "url": "%s",
            "type": "%s",
            "size": %d,
            "timestamp": %d
        }
        """.formatted(
                message.getConversation().getId(),
                message.getId(),
                user.getId(),
                attachment.getUrl(),
                attachment.getType(),
                attachment.getSize(),
                new Date().getTime()
        );

        broadcaster.broadcast(message.getConversation().getId(), json);
    }
}
