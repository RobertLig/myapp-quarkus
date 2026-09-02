package org.example.myapp.controller;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.example.myapp.service.MessageDeleteService;

@Path("/messages")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class MessageDeleteController {

    @Inject
    MessageDeleteService messageDeleteService;

    @DELETE
    @Path("/{messageId}/users/{userId}")
    public Response deleteMessage(
            @PathParam("messageId") Long messageId,
            @PathParam("userId") Long userId) {

        messageDeleteService.deleteMessage(messageId, userId);
        return Response.ok().build();
    }
}
