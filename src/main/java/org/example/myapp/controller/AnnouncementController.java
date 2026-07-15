package org.example.myapp.controller;

import org.example.myapp.dto.AnnouncementDTO;
import org.example.myapp.model.Announcement;
import org.example.myapp.service.AnnouncementService;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/announcements")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AnnouncementController {

    @Inject
    AnnouncementService announcementService;

    @GET
    public List<AnnouncementDTO> getAll() {
        return announcementService.findAll()
                .stream()
                .map(announcementService::toDTO)
                .toList();
    }

    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") Long id) {
        return announcementService.findById(id)
                .map(announcementService::toDTO)
                .map(Response::ok)
                .orElse(Response.status(Response.Status.NOT_FOUND))
                .build();
    }

    @POST
    public AnnouncementDTO create(AnnouncementDTO dto) {
        Announcement entity = announcementService.toEntity(dto);
        Announcement saved = announcementService.create(entity);
        return announcementService.toDTO(saved);
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Long id) {
        boolean deleted = announcementService.delete(id);
        return deleted
                ? Response.noContent().build()
                : Response.status(Response.Status.NOT_FOUND).build();
    }
}
