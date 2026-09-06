package org.example.myapp.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import org.example.myapp.model.EventType;
import org.example.myapp.model.Message;
import org.example.myapp.model.MessageEvent;
import org.example.myapp.repository.MessageEventRepository;
import org.example.myapp.repository.MessageRepository;
import org.example.myapp.ws.ChatBroadcaster;
import org.example.myapp.ws.DeletedEventPayloadBuilder;

import java.util.Date;

@ApplicationScoped
public class MessageDeleteService {

    @Inject
    MessageRepository messageRepository;

    @Inject
    MessageEventRepository eventRepository;

    @Inject
    ChatBroadcaster broadcaster;

    public void deleteMessage(Long messageId, Long userId) {

        Message message = messageRepository.findById(messageId);
        if (message == null) {
            throw new WebApplicationException("error.message.notfound", 404);
        }

        Long senderId = message.getSender().getId();
        Long conversationId = message.getConversation().getId();

        // Sender deletes → global delete
        if (senderId.equals(userId)) {
            message.setDeletedAt(new Date());
        } else {
            // Recipient deletes → hide only for themselves
            message.setDeletedForRecipientAt(new Date());
        }

        // Create delete event
        MessageEvent event = new MessageEvent();
        event.setType(EventType.DELETED);
        event.setUser(message.getSender());
        event.setMessage(message);
        event.setCreatedAt(new Date());

        eventRepository.persist(event);

        // Broadcast delete event
        String json = DeletedEventPayloadBuilder.build(event);
        broadcaster.broadcast(conversationId, json);
    }
}
