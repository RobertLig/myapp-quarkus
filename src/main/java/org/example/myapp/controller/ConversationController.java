package org.example.myapp.controller;

import io.quarkus.security.identity.SecurityIdentity;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.example.myapp.dto.*;
import org.example.myapp.mapper.ConversationMapper;
import org.example.myapp.model.Conversation;
import org.example.myapp.model.ConversationParticipant;
import org.example.myapp.service.ConversationService;
import org.example.myapp.service.MessageService;

import java.util.List;

@Path("/conversations")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ConversationController {

    @Inject
    ConversationService conversationService;

    @Inject
    MessageService messageService;

    @Inject
    ConversationMapper conversationMapper;

    @Inject
    SecurityIdentity identity;

    @POST
    @RolesAllowed({"USER", "ADMIN"})
    public Response createConversation(CreateConversationRequest req) {
        Long currentUserId = getLoggedInUserId();

        Conversation conversation = conversationService.createConversation(req.participantIds, req.announcementId, currentUserId);
        ConversationDTO dto = ConversationMapper.toDTO(conversation);

        return Response.ok(dto).build();
    }

    private Long getLoggedInUserId() {
        return Long.valueOf(identity.getPrincipal().getName());
    }

    @GET
    @Path("/list")
    @RolesAllowed({"USER", "ADMIN"})
    public Response listConversations() {
        // Match createConversation pattern: get ID from security context
        Long currentUserId = getLoggedInUserId();

        List<ConversationListItemDTO> dtos = conversationService.listConversations(currentUserId);

        return Response.ok(dtos).build();
    }

    @GET
    @Path("/{conversationId}/messages")
    public MessagePageDTO getMessages(
            @PathParam("conversationId") Long conversationId,
            @QueryParam("before") Long before,
            @QueryParam("limit") @DefaultValue("30") int limit) {

        return messageService.getPaginated(conversationId, before, limit);
    }

    @GET
    @Path("/{conversationId}/search")
    public MessageSearchDTO searchMessages(
            @PathParam("conversationId") Long conversationId,
            @QueryParam("query") String query,
            @QueryParam("before") Long before,
            @QueryParam("limit") @DefaultValue("30") int limit) {

        if (query == null || query.isBlank()) {
            throw new BadRequestException("query.required");
        }

        return messageService.search(conversationId, query, before, limit);
    }

    @GET
    @Path("/support")
    @RolesAllowed({"USER", "ADMIN"})
    public Response listSupportConversations() {
        // Get user/admin ID securely from the security context
        Long currentUserId = getLoggedInUserId();

        List<ConversationListItemDTO> dtos = conversationService.listSupportConversations(currentUserId);

        return Response.ok(dtos).build();
    }
}
