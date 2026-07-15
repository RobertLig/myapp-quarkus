package org.example.myapp.controller;

import org.example.myapp.dto.StopDTO;
import org.example.myapp.model.Stop;
import org.example.myapp.service.StopService;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

import jakarta.validation.Valid;

@Path("/stops")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class StopController {

    @Inject
    StopService stopService;

    @GET
    public List<StopDTO> getAll() {
        return stopService.findAll()
                .stream()
                .map(stopService::toDTO)
                .toList();
    }

    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") Long id) {
        return stopService.findById(id)
                .map(stopService::toDTO)
                .map(Response::ok)
                .orElse(Response.status(Response.Status.NOT_FOUND))
                .build();
    }

    @POST
    public StopDTO create(@Valid StopDTO dto) {
        Stop entity = stopService.toEntity(dto);
        Stop saved = stopService.create(entity);
        return stopService.toDTO(saved);
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Long id) {
        boolean deleted = stopService.delete(id);
        return deleted
                ? Response.noContent().build()
                : Response.status(Response.Status.NOT_FOUND).build();
    }
}
