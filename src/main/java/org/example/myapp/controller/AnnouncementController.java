package org.example.myapp.controller;

import org.example.myapp.dto.AnnouncementDTO;
import org.example.myapp.dto.PaginationResponse;
import org.example.myapp.model.Announcement;
import org.example.myapp.service.AnnouncementService;
import org.example.myapp.i18n.MessageService;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.validation.Valid;

@Path("/announcements")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AnnouncementController {

    @Inject
    AnnouncementService announcementService;

    @Inject
    MessageService messageService;

    @GET
    public PaginationResponse<AnnouncementDTO> list(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("10") int size) {

        return announcementService.getPaginated(page, size);
    }

    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") Long id, HttpHeaders headers) {
        return announcementService.findById(id)
                .map(announcementService::toAnnouncementDTO)
                .map(dto -> Response.ok(dto).build())
                .orElse(Response.status(Response.Status.NOT_FOUND)
                        .entity(
                                java.util.Map.of(
                                        "error", messageService.get("error.notfound", headers)
                                )
                        )
                        .build()
                );
    }

    @POST
    public Response create(@Valid AnnouncementDTO dto, HttpHeaders headers) {
        Announcement entity = announcementService.toAnnouncementEntity(dto);
        Announcement saved = announcementService.create(entity);

        return Response.ok(
                java.util.Map.of(
                        "message", messageService.get("announcement.created", headers),
                        "announcement", announcementService.toAnnouncementDTO(saved)

                )
        ).build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Long id, HttpHeaders headers) {
        boolean deleted = announcementService.delete(id);

        if (!deleted) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(
                            java.util.Map.of(
                                    "error", messageService.get("error.notfound", headers)
                            )
                    )
                    .build();
        }

        return Response.ok(
                java.util.Map.of(
                        "message", messageService.get("announcement.deleted", headers)
                )
        ).build();
    }
}
