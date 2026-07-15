package org.example.myapp.controller;

import org.example.myapp.dto.AnnouncementDimensionsDTO;
import org.example.myapp.model.AnnouncementDimensions;
import org.example.myapp.service.AnnouncementService;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/announcement-dimensions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AnnouncementDimensionsController {

    @Inject
    AnnouncementService announcementService;

    @POST
    public AnnouncementDimensionsDTO create(AnnouncementDimensionsDTO dto) {
        AnnouncementDimensions entity = announcementService.toEntity(dto);
        AnnouncementDimensions saved = entity; // embeddables are not persisted alone
        return announcementService.toDTO(saved);
    }

    @POST
    @Path("/convert")
    public AnnouncementDimensionsDTO convert(AnnouncementDimensionsDTO dto) {
        AnnouncementDimensions entity = announcementService.toEntity(dto);
        return announcementService.toDTO(entity);
    }
}
