package org.example.myapp.controller;

import org.example.myapp.dto.PhotoDTO;
import org.example.myapp.model.Photo;
import org.example.myapp.service.PhotoService;
import org.example.myapp.service.AnnouncementService;
import org.example.myapp.i18n.MessageService;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.HttpHeaders;

import jakarta.validation.Valid;

import java.util.List;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URLConnection;

@Path("/photos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PhotoController {

    @Inject
    PhotoService photoService;

    @Inject
    MessageService messageService;

    @Inject
    AnnouncementService announcementService;

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

    // --------- FILE UPLOAD WITH SIZE VALIDATION (500 KB) ---------

    @POST
    @Path("/upload/{announcementId}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response upload(@PathParam("announcementId") Long announcementId,
                           @FormParam("file") byte[] file,
                           HttpHeaders headers) {

        // Validate file presence
        if (file == null || file.length == 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(java.util.Map.of(
                            "error", messageService.get("photo.empty", headers)
                    ))
                    .build();
        }

        // Validate file size (max 500 KB)
        long maxSizeBytes = 500L * 1024L;
        if (file.length > maxSizeBytes) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(java.util.Map.of(
                            "error", messageService.get("photo.toobig", headers)
                    ))
                    .build();
        }

        // Detect MIME type
        String mime;
        try {
            mime = URLConnection.guessContentTypeFromStream(
                    new ByteArrayInputStream(file)
            );
        } catch (IOException e) {
            mime = null;
        }

        // Validate MIME type
        if (mime == null ||
                (!mime.equals("image/jpeg") &&
                        !mime.equals("image/jpg") &&
                        !mime.equals("image/png"))) {

            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(java.util.Map.of(
                            "error", messageService.get("photo.invalidtype", headers)
                    ))
                    .build();
        }

        // Fetch announcement
        var announcementOpt = announcementService.findById(announcementId);
        if (announcementOpt.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(java.util.Map.of(
                            "error", messageService.get("error.notfound", headers)
                    ))
                    .build();
        }

        var announcement = announcementOpt.get();

        // LIMIT: max 3 photos
        if (announcement.getPhotos().size() >= 3) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(java.util.Map.of(
                            "error", messageService.get("photo.limit", headers)
                    ))
                    .build();
        }

        // TODO: Upload file to S3/R2 and get URL
        String url = "https://example.com/photos/" + System.currentTimeMillis();

        // Create photo entity
        Photo photo = new Photo();
        photo.setUrl(url);

        Photo saved = photoService.create(photo);

        return Response.ok(
                java.util.Map.of(
                        "message", messageService.get("photo.uploaded", headers),
                        "photo", photoService.toDTO(saved)
                )
        ).build();
    }
}
