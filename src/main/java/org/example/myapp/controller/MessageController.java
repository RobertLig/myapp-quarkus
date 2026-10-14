package org.example.myapp.controller;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.annotation.security.RolesAllowed;

import org.example.myapp.dto.*;
import org.example.myapp.mapper.MessageMapper;
import org.example.myapp.model.Conversation;
import org.example.myapp.model.Message;
import org.example.myapp.service.MessageService;
import org.example.myapp.service.ConversationService;

import java.util.List;

import io.quarkus.security.identity.SecurityIdentity;

@Path("/messages")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class MessageController {

    @Inject
    SecurityIdentity identity;

    @Inject
    MessageService messageService;

    @Inject
    ConversationService conversationService;

    @POST
    @RolesAllowed({"USER", "ADMIN"})
    public Response sendMessage(@Valid SendMessageRequest req) {
        Long currentUserId = getLoggedInUserId();

        // Call service passing the authenticated user's ID as the sender
        MessageDTO dto = messageService.sendMessage(
                req.conversationId,
                currentUserId, // Use authenticated user ID instead of trusting request body payload
                req.content
        );

        return Response.status(Response.Status.CREATED).entity(dto).build();
    }

    private Long getLoggedInUserId() {
        return Long.valueOf(identity.getPrincipal().getName());
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
        MessageDTO dto = messageService.sendMessage(
                conversation.getId(),
                userId,
                req.content
        );

        return Response.ok(dto).build();
    }

    @POST
    @Path("/support/reply")
    public Response replyToSupport(AdminReplyRequest req) {

        // Admin replies inside an existing support conversation
        MessageDTO dto = messageService.sendMessage(
                req.conversationId,
                req.adminId,
                req.content
        );

        return Response.ok(dto).build();
    }
}
