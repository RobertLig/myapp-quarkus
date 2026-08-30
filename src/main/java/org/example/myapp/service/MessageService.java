package org.example.myapp.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import org.example.myapp.model.Conversation;
import org.example.myapp.model.ConversationParticipant;
import org.example.myapp.model.Message;
import org.example.myapp.model.User;
import org.example.myapp.repository.ConversationRepository;
import org.example.myapp.repository.MessageRepository;
import org.example.myapp.repository.UserBlockRepository;
import org.example.myapp.repository.UserRepository;

import java.util.Date;

@ApplicationScoped
public class MessageService {

    @Inject
    ConversationRepository conversationRepository;

    @Inject
    MessageRepository messageRepository;

    @Inject
    UserRepository userRepository;

    @Inject
    UserBlockRepository userBlockRepository;

    public Message sendMessage(Long conversationId, Long senderId, String content) {

        // Load conversation
        Conversation conversation = conversationRepository.findById(conversationId);
        if (conversation == null) {
            throw new WebApplicationException("Conversation not found", 404);
        }

        // Load sender
        User sender = userRepository.findById(senderId);
        if (sender == null) {
            throw new WebApplicationException("Sender not found", 404);
        }

        // Check if sender is a participant
        boolean isParticipant = conversation.getParticipants().stream()
                .anyMatch(cp -> cp.getUser().getId().equals(senderId));

        if (!isParticipant) {
            throw new WebApplicationException("Sender is not a participant", 403);
        }

        // Check if sender is blocked by any participant
        for (ConversationParticipant cp : conversation.getParticipants()) {
            Long otherUserId = cp.getUser().getId();

            if (!otherUserId.equals(senderId)) {
                if (userBlockRepository.exists(otherUserId, senderId)) {
                    throw new WebApplicationException("Sender is blocked", 403);
                }
            }
        }

        // Create message
        Message message = new Message();
        message.setConversation(conversation);
        message.setSender(sender);
        message.setContent(content);
        message.setCreatedAt(new Date());

        messageRepository.persist(message);

        return message;
    }
}
