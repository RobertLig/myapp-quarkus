package org.example.myapp.controller;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.example.myapp.dto.MessageEventRequest;
import org.example.myapp.dto.MessageEventResponse;
import org.example.myapp.mapper.MessageEventMapper;
import org.example.myapp.model.MessageEvent;
import org.example.myapp.service.MessageEventService;

@Path("/events")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class MessageEventController {

    @Inject
    MessageEventService eventService;

    @POST
    public Response createEvent(MessageEventRequest req) {

        MessageEvent event = eventService.createEvent(req);

        MessageEventResponse dto = MessageEventMapper.toDTO(event);

        return Response.ok(dto).build();
    }
}
