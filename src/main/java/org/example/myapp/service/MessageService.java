package org.example.myapp.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import org.example.myapp.dto.MessageDTO;
import org.example.myapp.dto.MessagePageDTO;
import org.example.myapp.dto.MessageSearchDTO;
import org.example.myapp.mapper.MessageMapper;
import org.example.myapp.model.*;
import org.example.myapp.repository.ConversationRepository;
import org.example.myapp.repository.MessageRepository;
import org.example.myapp.repository.UserBlockRepository;
import org.example.myapp.repository.UserRepository;
import org.example.myapp.ws.ChatBroadcaster;
import org.example.myapp.ws.DeliveredReceiptPayloadBuilder;
import org.example.myapp.ws.MessagePayloadBuilder;

import java.util.Date;
import java.util.List;

import org.example.myapp.repository.MessageEventRepository;

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

    @Inject
    ChatBroadcaster broadcaster;

    @Inject
    MessageEventRepository eventRepository;

    public Message sendMessage(Long conversationId, Long senderId, String content) {

        // Load conversation
        Conversation conversation = conversationRepository.findById(conversationId);
        if (conversation == null) {
            throw new WebApplicationException("error.conversation.notfound", 404);
        }

        // Prevent sending messages to yourself
        long distinctUsers = conversation.getParticipants().stream()
                .map(cp -> cp.getUser().getId())
                .distinct()
                .count();

        if (distinctUsers == 1) {
            throw new WebApplicationException("error.message.self", 400);
        }

        // Load sender
        User sender = userRepository.findById(senderId);
        if (sender == null) {
            throw new WebApplicationException("error.sender.notfound", 404);
        }

        // Check if sender is a participant
        boolean isParticipant = conversation.getParticipants().stream()
                .anyMatch(cp -> cp.getUser().getId().equals(senderId));

        if (!isParticipant) {
            throw new WebApplicationException("error.message.notparticipant", 403);
        }

        // Check if sender is blocked by any participant
        for (ConversationParticipant cp : conversation.getParticipants()) {
            Long otherUserId = cp.getUser().getId();

            if (!otherUserId.equals(senderId)) {
                if (userBlockRepository.exists(otherUserId, senderId)) {
                    throw new WebApplicationException("error.message.blocked", 403);
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

        for (ConversationParticipant cp : conversation.getParticipants()) {
            Long participantId = cp.getUser().getId();

            if (!participantId.equals(senderId)) {
                cp.setUnreadCount(cp.getUnreadCount() + 1);
            }
        }

        // Convert to DTO
        MessageDTO dto = MessageMapper.toDTO(message);

        // Broadcast the message itself
        String messageJson = MessagePayloadBuilder.build(dto);
        broadcaster.broadcast(conversationId, messageJson);

        // Create DELIVERED event
        MessageEvent delivered = new MessageEvent();
        delivered.setType(EventType.DELIVERED);
        delivered.setUser(sender); // sender sees "delivered"
        delivered.setMessage(message);
        delivered.setCreatedAt(new Date());

        eventRepository.persist(delivered);

        // Broadcast delivered receipt
        String deliveredJson = DeliveredReceiptPayloadBuilder.build(delivered);
        broadcaster.broadcast(conversationId, deliveredJson);

        return message;
    }

    public void deleteMessage(Long messageId, Long userId, String mode) {

        Message message = messageRepository.findById(messageId);
        if (message == null) {
            throw new WebApplicationException("error.message.notfound", 404);
        }

        User user = userRepository.findById(userId);
        if (user == null) {
            throw new WebApplicationException("error.user.notfound", 404);
        }

        switch (mode.toLowerCase()) {

            case "recipient":
                boolean isRecipient = message.getConversation().getParticipants().stream()
                        .anyMatch(cp -> cp.getUser().getId().equals(userId));

                if (!isRecipient) {
                    throw new WebApplicationException("error.message.not.recipient", 403);
                }
                message.setDeletedForRecipientAt(new Date());
                break;

            case "global":
                // Sender deletes → full removal
                if (!message.getSender().getId().equals(userId)) {
                    throw new WebApplicationException("error.message.not.sender", 403);
                }

                messageRepository.delete(message);
                break;

            default:
                throw new WebApplicationException("error.message.delete.mode", 400);
        }

        messageRepository.persist(message);
    }

    public List<Message> listMessages(Long conversationId, Long userId) {

        // Validate conversation
        Conversation conversation = conversationRepository.findById(conversationId);
        if (conversation == null) {
            throw new WebApplicationException("error.conversation.notfound", 404);
        }

        // Validate user
        User user = userRepository.findById(userId);
        if (user == null) {
            throw new WebApplicationException("error.user.notfound", 404);
        }

        // Check if user is participant
        boolean isParticipant = conversation.getParticipants().stream()
                .anyMatch(cp -> cp.getUser().getId().equals(userId));

        if (!isParticipant) {
            throw new WebApplicationException("error.message.notparticipant", 403);
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

    public MessagePageDTO getPaginated(Long conversationId, Long before, int limit) {

        List<Message> entities = messageRepository.findPaginated(conversationId, before, limit);

        List<MessageDTO> dtos = entities.stream()
                .map(MessageMapper::toDTO)
                .toList();

        // Check if more messages exist
        boolean hasMore;

        if (entities.isEmpty()) {
            hasMore = false;
        } else {
            Date oldestLoaded = entities.get(entities.size() - 1).getCreatedAt();

            long countOlder = messageRepository.count(
                    "conversation.id = ?1 AND createdAt < ?2",
                    conversationId, oldestLoaded
            );

            hasMore = countOlder > 0;
        }

        MessagePageDTO page = new MessagePageDTO();
        page.messages = dtos;
        page.hasMore = hasMore;

        return page;
    }

    public MessageSearchDTO search(Long conversationId, String query, Long before, int limit) {

        List<Message> entities = messageRepository.search(conversationId, query, before, limit);

        List<MessageDTO> dtos = entities.stream()
                .map(MessageMapper::toDTO)
                .toList();

        boolean hasMore;

        if (entities.isEmpty()) {
            hasMore = false;
        } else {
            Date oldestLoaded = entities.get(entities.size() - 1).getCreatedAt();

            long countOlder = messageRepository.count(
                    "conversation.id = ?1 AND content ILIKE ?2 AND createdAt < ?3",
                    conversationId, "%" + query + "%", oldestLoaded
            );

            hasMore = countOlder > 0;
        }

        MessageSearchDTO dto = new MessageSearchDTO();
        dto.messages = dtos;
        dto.hasMore = hasMore;

        return dto;
    }

}
