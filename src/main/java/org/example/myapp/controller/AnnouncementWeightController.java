package org.example.myapp.controller;

import org.example.myapp.dto.AnnouncementWeightDTO;
import org.example.myapp.model.AnnouncementWeight;
import org.example.myapp.service.AnnouncementService;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

@Path("/announcement-weight")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AnnouncementWeightController {

    @Inject
    AnnouncementService announcementService;

    @POST
    public AnnouncementWeightDTO create(AnnouncementWeightDTO dto) {
        AnnouncementWeight entity = announcementService.toEntity(dto);
        return announcementService.toDTO(entity);
    }

    @POST
    @Path("/convert")
    public AnnouncementWeightDTO convert(AnnouncementWeightDTO dto) {
        AnnouncementWeight entity = announcementService.toEntity(dto);
        return announcementService.toDTO(entity);
    }
}
