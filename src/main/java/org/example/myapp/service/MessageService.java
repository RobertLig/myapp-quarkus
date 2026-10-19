package org.example.myapp.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import org.example.myapp.dto.MessageDTO;
import org.example.myapp.dto.MessagePageDTO;
import org.example.myapp.dto.MessageSearchDTO;
import org.example.myapp.exception.DomainException;
import org.example.myapp.exception.EntityNotFoundException;
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

    @Inject
    RateLimitService rateLimitService;

    @Transactional
    public MessageDTO sendMessage(Long conversationId, Long senderId, String content) {

        // Rate limit per user for messaging
        if (!rateLimitService.allowMessage(senderId)) {
            throw DomainException.tooManyRequests("error.rate.limit");
        }

        // Load conversation
        Conversation conversation = conversationRepository.findById(conversationId);
        if (conversation == null) {
            throw new EntityNotFoundException("error.conversation.notfound");
        }

        // Prevent sending messages to yourself
        long distinctUsers = conversation.getParticipants().stream()
                .map(cp -> cp.getUser().getId())
                .distinct()
                .count();

        if (distinctUsers == 1) {
            throw DomainException.badRequest("error.message.self");
        }

        // Load sender
        User sender = userRepository.findById(senderId);
        if (sender == null) {
            throw new EntityNotFoundException("error.sender.notfound");
        }

        // Check if sender is a participant
        boolean isParticipant = conversation.getParticipants().stream()
                .anyMatch(cp -> cp.getUser().getId().equals(senderId));

        if (!isParticipant) {
            throw DomainException.forbidden("error.message.notparticipant");
        }

        // Check if sender is blocked by any participant
        for (ConversationParticipant cp : conversation.getParticipants()) {
            Long otherUserId = cp.getUser().getId();

            if (!otherUserId.equals(senderId)) {
                if (userBlockRepository.exists(otherUserId, senderId)) {
                    throw DomainException.forbidden("error.message.blocked");
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

        return dto;
    }

    public MessagePageDTO getPaginated(Long conversationId, Long loggedInUserId, Long before, int limit) {
        // 1. Validate conversation exists
        Conversation conversation = conversationRepository.findById(conversationId);
        if (conversation == null) {
            throw new EntityNotFoundException("error.conversation.notfound");
        }

        // 2. Validate logged-in user exists
        User user = userRepository.findById(loggedInUserId);
        if (user == null) {
            throw new EntityNotFoundException("error.user.notfound");
        }

        // 3. Check participant status
        boolean isParticipant = conversation.getParticipants().stream()
                .anyMatch(cp -> cp.getUser().getId().equals(loggedInUserId));

        if (!isParticipant) {
            throw DomainException.forbidden("error.message.notparticipant");
        }

        // 4. Fetch paginated entities
        List<Message> entities = messageRepository.findPaginated(conversationId, before, limit);

        // 5. Apply recipient deletion filter and map to DTOs
        List<MessageDTO> dtos = entities.stream()
                .filter(m -> {
                    if (m.getDeletedForRecipientAt() != null &&
                            !m.getSender().getId().equals(loggedInUserId)) {
                        return false;
                    }
                    return true;
                })
                .map(MessageMapper::toDTO)
                .toList();

        // 6. Check if more messages exist
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
