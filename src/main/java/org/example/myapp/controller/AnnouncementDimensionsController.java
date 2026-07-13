package org.example.myapp.controller;

import org.example.myapp.model.AnnouncementDimensions;
import org.example.myapp.service.AnnouncementDimensionsService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/announcement-dimensions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AnnouncementDimensionsController {

    @Inject
    AnnouncementDimensionsService dimensionsService;

    @GET
    public Response getAll() {
        return Response.ok(dimensionsService.findAll()).build();
    }

    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") Long id) {
        return dimensionsService.findById(id)
                .map(Response::ok)
                .orElse(Response.status(Response.Status.NOT_FOUND))
                .build();
    }

    @POST
    public Response create(AnnouncementDimensions dimensions) {
        return Response.ok(dimensionsService.create(dimensions)).build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Long id) {
        boolean deleted = dimensionsService.delete(id);
        return deleted
                ? Response.noContent().build()
                : Response.status(Response.Status.NOT_FOUND).build();
    }
}
