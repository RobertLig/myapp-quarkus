package org.example.myapp.controller;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.example.myapp.dto.PresignResponseDTO;
import org.example.myapp.service.AttachmentPresignService;

@Path("/attachments")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AttachmentController {

    @Inject
    AttachmentPresignService presignService;

    public static class PresignRequestDTO {
        public String contentType; // e.g. "image/png", "application/pdf"
    }

    @POST
    @Path("/presign")
    public Response presign(PresignRequestDTO req) {

        if (req.contentType == null || req.contentType.isBlank()) {
            throw new BadRequestException("contentType.required");
        }

        PresignResponseDTO dto = presignService.presign(req.contentType);
        return Response.ok(dto).build();
    }
}
