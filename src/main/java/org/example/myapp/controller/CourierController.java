package org.example.myapp.controller;

import org.example.myapp.dto.CourierDTO;
import org.example.myapp.model.Courier;
import org.example.myapp.service.CourierService;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

import jakarta.validation.Valid;

@Path("/couriers")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CourierController {

    @Inject
    CourierService courierService;

    @GET
    public List<CourierDTO> getAll() {
        return courierService.findAll()
                .stream()
                .map(courierService::toDTO)
                .toList();
    }

    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") Long id) {
        return courierService.findById(id)
                .map(courierService::toDTO)
                .map(Response::ok)
                .orElse(Response.status(Response.Status.NOT_FOUND))
                .build();
    }

    @POST
    public CourierDTO create(@Valid CourierDTO dto) {
        Courier entity = courierService.toEntity(dto);
        Courier saved = courierService.create(entity);
        return courierService.toDTO(saved);
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Long id) {
        boolean deleted = courierService.delete(id);
        return deleted
                ? Response.noContent().build()
                : Response.status(Response.Status.NOT_FOUND).build();
    }
}
