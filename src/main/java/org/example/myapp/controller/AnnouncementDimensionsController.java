package org.example.myapp.controller;

import org.example.myapp.dto.AnnouncementDimensionsDTO;
import org.example.myapp.model.AnnouncementDimensions;
import org.example.myapp.service.AnnouncementService;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.validation.Valid;

@Path("/announcement-dimensions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AnnouncementDimensionsController {

    @Inject
    AnnouncementService announcementService;

    @POST
    public AnnouncementDimensionsDTO create(@Valid AnnouncementDimensionsDTO dto) {
        AnnouncementDimensions entity =
                announcementService.toAnnouncementDimensionsEntity(dto);

        return announcementService.toAnnouncementDimensionsDTO(entity);
    }

    @POST
    @Path("/convert")
    public AnnouncementDimensionsDTO convert(@Valid AnnouncementDimensionsDTO dto) {
        AnnouncementDimensions entity =
                announcementService.toAnnouncementDimensionsEntity(dto);

        return announcementService.toAnnouncementDimensionsDTO(entity);
    }
}
