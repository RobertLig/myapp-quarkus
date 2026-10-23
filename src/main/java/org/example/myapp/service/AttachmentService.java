package org.example.myapp.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.example.myapp.dto.AttachmentRequest;
import org.example.myapp.exception.DomainException;
import org.example.myapp.exception.EntityNotFoundException;
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

    @Inject
    ImageLimitService imageLimitService;

    @Transactional
    public void addAttachment(Long messageId, AttachmentRequest req, Long currentUserId) {

        Message message = messageRepository.findById(messageId);
        if (message == null) {
            throw new EntityNotFoundException("error.message.notfound");
        }

        User user = userRepository.findById(currentUserId);
        if (user == null) {
            throw new EntityNotFoundException("error.user.notfound");
        }

        // Validate user is a participant
        boolean isParticipant = message.getConversation().getParticipants().stream()
                .anyMatch(cp -> cp.getUser().getId().equals(currentUserId));

        if (!isParticipant) {
            throw DomainException.forbidden("error.message.notparticipant");
        }

        // Limit Check
        if (!imageLimitService.canAddMessageAttachment(message)) {
            throw DomainException.tooManyRequests("error.attachment.limit");
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
