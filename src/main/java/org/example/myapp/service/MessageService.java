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
import java.util.List;

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

    public void deleteMessage(Long messageId, Long userId, String mode) {

        Message message = messageRepository.findById(messageId);
        if (message == null) {
            throw new WebApplicationException("Message not found", 404);
        }

        User user = userRepository.findById(userId);
        if (user == null) {
            throw new WebApplicationException("User not found", 404);
        }

        switch (mode.toLowerCase()) {

            case "recipient":
                boolean isRecipient = message.getConversation().getParticipants().stream()
                        .anyMatch(cp -> cp.getUser().getId().equals(userId));

                if (!isRecipient) {
                    throw new WebApplicationException("Not message recipient", 403);
                }
                message.setDeletedForRecipientAt(new Date());
                break;

            case "global":
                // Sender deletes → full removal
                if (!message.getSender().getId().equals(userId)) {
                    throw new WebApplicationException("Not message sender", 403);
                }

                messageRepository.delete(message);
                break;

            default:
                throw new WebApplicationException("Invalid delete mode", 400);
        }

        messageRepository.persist(message);
    }

    public List<Message> listMessages(Long conversationId, Long userId) {

        // Validate conversation
        Conversation conversation = conversationRepository.findById(conversationId);
        if (conversation == null) {
            throw new WebApplicationException("Conversation not found", 404);
        }

        // Validate user
        User user = userRepository.findById(userId);
        if (user == null) {
            throw new WebApplicationException("User not found", 404);
        }

        // Check if user is participant
        boolean isParticipant = conversation.getParticipants().stream()
                .anyMatch(cp -> cp.getUser().getId().equals(userId));

        if (!isParticipant) {
            throw new WebApplicationException("Not a conversation participant", 403);
        }

        // Load all messages
        List<Message> messages = messageRepository.findByConversation(conversationId);

        // Filter out messages deleted for this user
        return messages.stream()
                .filter(m -> {

                    // Sender deleted globally → message removed from DB
                    // (so it never appears here)

                    // Recipient deleted → hide only for that recipient
                    if (m.getDeletedForRecipientAt() != null &&
                            !m.getSender().getId().equals(userId)) {
                        return false;
                    }

                    return true;
                })
                .toList();
    }
}
