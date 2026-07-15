package org.example.myapp.controller;

import org.example.myapp.dto.SenderDTO;
import org.example.myapp.model.Sender;
import org.example.myapp.service.SenderService;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

import jakarta.validation.Valid;

@Path("/senders")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SenderController {

    @Inject
    SenderService senderService;

    @GET
    public List<SenderDTO> getAll() {
        return senderService.findAll()
                .stream()
                .map(senderService::toDTO)
                .toList();
    }

    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") Long id) {
        return senderService.findById(id)
                .map(senderService::toDTO)
                .map(Response::ok)
                .orElse(Response.status(Response.Status.NOT_FOUND))
                .build();
    }

    @POST
    public SenderDTO create(@Valid SenderDTO dto) {
        Sender entity = senderService.toEntity(dto);
        Sender saved = senderService.create(entity);
        return senderService.toDTO(saved);
    }


    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Long id) {
        boolean deleted = senderService.delete(id);
        return deleted
                ? Response.noContent().build()
                : Response.status(Response.Status.NOT_FOUND).build();
    }
}
