package org.example.myapp.controller;

import io.quarkus.security.identity.SecurityIdentity;
import jakarta.annotation.security.RolesAllowed;
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

    @Inject
    SecurityIdentity identity;

    private Long getLoggedInUserId() {
        return Long.valueOf(identity.getPrincipal().getName());
    }

    @POST
    @Path("/attachments")
    @RolesAllowed({"USER", "ADMIN"})
    public Response addAttachment(@Valid AttachmentRequest req) {
        Long currentUserId = getLoggedInUserId();
        attachmentService.addAttachment(req.messageId, req, currentUserId);
        return Response.ok().build();
    }
}
