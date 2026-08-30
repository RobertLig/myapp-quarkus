package org.example.myapp.controller;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.example.myapp.dto.ConversationDTO;
import org.example.myapp.dto.CreateConversationRequest;
import org.example.myapp.mapper.ConversationMapper;
import org.example.myapp.model.Conversation;
import org.example.myapp.service.ConversationService;

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
}
