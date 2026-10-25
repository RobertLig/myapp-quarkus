package org.example.myapp.controller;

import io.quarkus.security.identity.SecurityIdentity;
import jakarta.inject.Inject;
import jakarta.annotation.security.RolesAllowed;
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

    @Inject
    SecurityIdentity identity;

    private Long getLoggedInUserId() {
        return Long.valueOf(identity.getPrincipal().getName());
    }

    public static class PresignRequestDTO {
        @jakarta.validation.constraints.NotBlank
        @jakarta.validation.constraints.Pattern(regexp = "^[a-zA-Z]+/[a-zA-Z0-9+\\-._]+$", message = "contentType.invalid")
        public String contentType; // e.g. "image/png", "application/pdf"

        @jakarta.validation.constraints.NotNull
        @jakarta.validation.constraints.Min(value = 1)
        @jakarta.validation.constraints.Max(value = 10485760) // 10MB max
        public Long fileSize; // file size in bytes

        @jakarta.validation.constraints.NotNull
        public Long messageId;
    }

    @POST
    @Path("/presign")
    @RolesAllowed({"USER", "ADMIN"})
    public Response presign(PresignRequestDTO req) {
        Long currentUserId = getLoggedInUserId();
        PresignResponseDTO dto = presignService.presign(req.contentType, req.fileSize, req.messageId, currentUserId);
        return Response.ok(dto).build();
    }
}
