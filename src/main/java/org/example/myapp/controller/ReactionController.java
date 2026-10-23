package org.example.myapp.controller;

import io.quarkus.security.identity.SecurityIdentity;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
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

    @Inject
    SecurityIdentity identity;

    private Long getLoggedInUserId() {
        return Long.valueOf(identity.getPrincipal().getName());
    }

    @POST
    @Path("/reactions")
    @RolesAllowed({"USER", "ADMIN"})
    public Response react(@Valid ReactionRequest req) {

        Long currentUserId = getLoggedInUserId();
        reactionService.react(req.messageId, req, currentUserId);
        return Response.ok().build();
    }
}
