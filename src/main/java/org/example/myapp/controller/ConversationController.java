package org.example.myapp.controller;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.example.myapp.dto.ConversationDTO;
import org.example.myapp.dto.ConversationListItemDTO;
import org.example.myapp.dto.CreateConversationRequest;
import org.example.myapp.mapper.ConversationMapper;
import org.example.myapp.model.Conversation;
import org.example.myapp.model.ConversationParticipant;
import org.example.myapp.service.ConversationService;

import java.util.List;

@Path("/conversations")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ConversationController {

    @Inject
    ConversationService conversationService;

    @POST
    public Response createConversation(CreateConversationRequest req) {

        Conversation conversation = conversationService.createConversation(req.participantIds);

        ConversationDTO dto = ConversationMapper.toDTO(conversation);

        return Response.ok(dto).build();
    }

    @GET
    @Path("/list/{userId}")
    public Response listConversations(@PathParam("userId") Long userId) {

        List<ConversationParticipant> cps = conversationService.listConversations(userId);

        List<ConversationListItemDTO> dtos = cps.stream()
                .map(ConversationMapper::toListItemDTO)
                .toList();

        return Response.ok(dtos).build();
    }
}
