package org.example.myapp.controller;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.example.myapp.dto.AttachmentRequest;
import org.example.myapp.service.AttachmentService;

@Path("/messages")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class MessageAttachmentController {

    @Inject
    AttachmentService attachmentService;

    @POST
    @Path("/attachments")
    public Response addAttachment(@Valid AttachmentRequest req) {
        attachmentService.addAttachment(req.messageId, req);
        return Response.ok().build();
    }
}
