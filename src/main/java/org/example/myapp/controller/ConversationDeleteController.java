package org.example.myapp.controller;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.example.myapp.service.ConversationService;

@Path("/conversations")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ConversationDeleteController {

    @Inject
    ConversationService conversationService;

    @DELETE
    @Path("/{conversationId}/users/{userId}")
    public Response deleteConversation(
            @PathParam("conversationId") Long conversationId,
            @PathParam("userId") Long userId) {

        conversationService.deleteConversationForUser(conversationId, userId);
        return Response.ok().build();
    }
}
