package org.example.myapp.controller;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.example.myapp.dto.TypingEventRequest;
import org.example.myapp.service.TypingEventService;

import io.quarkus.security.identity.SecurityIdentity;
import jakarta.annotation.security.RolesAllowed;

@Path("/typing")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class TypingEventController {

    @Inject
    TypingEventService typingEventService;

    @Inject
    SecurityIdentity identity;

    private Long getLoggedInUserId() {
        return Long.valueOf(identity.getPrincipal().getName());
    }

    @POST
    @RolesAllowed({"USER", "ADMIN"})
    public Response typing(@Valid TypingEventRequest req) {
        Long currentUserId = getLoggedInUserId();
        typingEventService.handleTypingEvent(req, currentUserId);
        return Response.ok().build();
    }
}
