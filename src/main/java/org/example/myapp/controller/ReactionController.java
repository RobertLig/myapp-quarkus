package org.example.myapp.controller;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.example.myapp.dto.ReactionRequest;
import org.example.myapp.service.ReactionService;

@Path("/messages")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ReactionController {

    @Inject
    ReactionService reactionService;

    @POST
    @Path("/{messageId}/reactions")
    public Response react(
            @PathParam("messageId") Long messageId,
            ReactionRequest req) {

        reactionService.react(messageId, req);
        return Response.ok().build();
    }
}
