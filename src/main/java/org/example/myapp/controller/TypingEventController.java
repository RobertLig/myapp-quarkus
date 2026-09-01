package org.example.myapp.controller;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.example.myapp.dto.TypingEventRequest;
import org.example.myapp.service.TypingEventService;

@Path("/typing")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class TypingEventController {

    @Inject
    TypingEventService typingEventService;

    @POST
    public Response typing(TypingEventRequest req) {
        typingEventService.handleTypingEvent(req);
        return Response.ok().build();
    }
}
