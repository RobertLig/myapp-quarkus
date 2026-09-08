package org.example.myapp.controller;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.example.myapp.dto.*;
import org.example.myapp.mapper.MessageMapper;
import org.example.myapp.model.Conversation;
import org.example.myapp.model.Message;
import org.example.myapp.service.MessageService;
import org.example.myapp.service.ConversationService;

import java.util.List;

@Path("/messages")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class MessageController {

    @Inject
    MessageService messageService;

    @Inject
    ConversationService conversationService;

    @POST
    public Response sendMessage(SendMessageRequest req) {

        Message message = messageService.sendMessage(
                req.conversationId,
                req.senderId,
                req.content
        );

        MessageDTO dto = MessageMapper.toDTO(message);

        return Response.ok(dto).build();
    }

    @POST
    @Path("/delete")
    public Response deleteMessage(DeleteMessageRequest req) {

        messageService.deleteMessage(req.messageId, req.userId, req.mode);

        DeleteMessageResponse res = new DeleteMessageResponse();
        res.messageId = req.messageId;
        res.status = "deleted";

        return Response.ok(res).build();
    }

    @POST
    @Path("/list")
    public Response listMessages(ListMessagesRequest req) {

        List<Message> messages = messageService.listMessages(
                req.conversationId,
                req.userId
        );

        List<MessageDTO> dtos = messages.stream()
                .map(MessageMapper::toDTO)
                .toList();

        return Response.ok(dtos).build();
    }

    @POST
    @Path("/support/send")
    public Response sendSupportMessage(SendSupportMessageRequest req) {

        Long userId = req.userId;      // authenticated user
        Long adminId = req.adminId;    // your admin user ID

        // 1. Find or create support conversation
        Conversation conversation =
                conversationService.getOrCreateSupportConversation(userId, adminId);

        // 2. Send message using existing messaging logic
        Message message = messageService.sendMessage(
                conversation.getId(),
                userId,
                req.content
        );

        // 3. Convert to DTO
        MessageDTO dto = MessageMapper.toDTO(message);

        return Response.ok(dto).build();
    }

    @POST
    @Path("/support/reply")
    public Response replyToSupport(AdminReplyRequest req) {

        // Admin replies inside an existing support conversation
        Message message = messageService.sendMessage(
                req.conversationId,
                req.adminId,
                req.content
        );

        MessageDTO dto = MessageMapper.toDTO(message);

        return Response.ok(dto).build();
    }
}
