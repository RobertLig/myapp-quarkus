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
import org.example.myapp.service.MessageService;
import org.example.myapp.service.ConversationService;

import io.quarkus.security.identity.SecurityIdentity;

@Path("/messages")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class MessageController {

    // Default admin ID (1L)
    private static final Long DEFAULT_ADMIN_ID = 1L;

    @Inject
    SecurityIdentity identity;

    @Inject
    MessageService messageService;

    @Inject
    ConversationService conversationService;

    private Long getLoggedInUserId() {
        return Long.valueOf(identity.getPrincipal().getName());
    }

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

    @POST
    @Path("/support/send")
    @RolesAllowed({"USER", "ADMIN"})
    public Response sendSupportMessage(@Valid SendSupportMessageRequest req) {
        Long currentUserId = getLoggedInUserId();

        MessageDTO dto = conversationService.sendSupportMessage(
                currentUserId,
                DEFAULT_ADMIN_ID,
                req.content
        );

        return Response.ok(dto).build();
    }

    @POST
    @Path("/support/reply")
    @RolesAllowed({"ADMIN"})
    public Response replyToSupport(@Valid AdminReplyRequest req) {
        Long currentUserId = getLoggedInUserId();

        MessageDTO dto = conversationService.replyToSupport(
                req.conversationId,
                currentUserId,
                req.content
        );

        return Response.ok(dto).build();
    }
}
