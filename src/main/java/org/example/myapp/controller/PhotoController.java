package org.example.myapp.controller;

import org.example.myapp.dto.PhotoDTO;
import org.example.myapp.model.Photo;
import org.example.myapp.service.PhotoService;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import jakarta.validation.Valid;

import java.util.List;

@Path("/photos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PhotoController {

    @Inject
    PhotoService photoService;

    @GET
    public List<PhotoDTO> getAll() {
        return photoService.findAll()
                .stream()
                .map(photoService::toDTO)
                .toList();
    }

    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") Long id) {
        return photoService.findById(id)
                .map(photoService::toDTO)
                .map(Response::ok)
                .orElse(Response.status(Response.Status.NOT_FOUND))
                .build();
    }

    @POST
    public PhotoDTO create(@Valid PhotoDTO dto) {
        Photo entity = photoService.toEntity(dto);
        Photo saved = photoService.create(entity);
        return photoService.toDTO(saved);
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Long id) {
        boolean deleted = photoService.delete(id);
        return deleted
                ? Response.noContent().build()
                : Response.status(Response.Status.NOT_FOUND).build();
    }
}
