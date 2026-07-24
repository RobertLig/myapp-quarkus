package org.example.myapp.controller;

import org.example.myapp.dto.AnnouncementDTO;
import org.example.myapp.dto.PaginationResponse;
import org.example.myapp.dto.PhotoDTO;
import org.example.myapp.dto.AnnouncementSearchDTO;
import org.example.myapp.model.Announcement;
import org.example.myapp.model.Photo;
import org.example.myapp.service.AnnouncementService;
import org.example.myapp.service.PhotoService;
import org.example.myapp.service.ImageStoreService;
import org.example.myapp.service.ImageLimitService;
import org.example.myapp.i18n.MessageService;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.validation.Valid;

import java.util.List;

@Path("/announcements")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AnnouncementController {

    @Inject
    AnnouncementService announcementService;

    @Inject
    PhotoService photoService;

    @Inject
    ImageStoreService imageStoreService;

    @Inject
    ImageLimitService imageLimitService;

    @Inject
    MessageService messageService;

    // ------------------------------------------------------------
    // CRUD
    // ------------------------------------------------------------

    @GET
    public PaginationResponse<AnnouncementDTO> list(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("10") int size) {

        return announcementService.getPaginated(page, size);
    }

    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") Long id, HttpHeaders headers) {
        return announcementService.findById(id)
                .map(announcementService::toAnnouncementDTO)
                .map(dto -> Response.ok(dto).build())
                .orElse(Response.status(Response.Status.NOT_FOUND)
                        .entity(java.util.Map.of(
                                "error", messageService.get("error.notfound", headers)
                        ))
                        .build());
    }

    @POST
    public Response create(@Valid AnnouncementDTO dto, HttpHeaders headers) {

        Announcement saved = announcementService.createAnnouncement(dto);

        return Response.ok(java.util.Map.of(
                "message", messageService.get("announcement.created", headers),
                "announcement", announcementService.toAnnouncementDTO(saved)
        )).build();
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") Long id,
                           @Valid AnnouncementDTO dto,
                           HttpHeaders headers) {

        var announcementOpt = announcementService.findById(id);

        if (announcementOpt.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(java.util.Map.of(
                            "error", messageService.get("error.notfound", headers)
                    ))
                    .build();
        }

        Announcement existing = announcementOpt.get();

        // Ownership check
        Response ownership = checkOwnership(dto.userId, existing, headers);
        if (ownership != null) return ownership;

        try {
            Announcement updated = announcementService.update(existing, dto);

            return Response.ok(java.util.Map.of(
                    "message", messageService.get("announcement.updated", headers),
                    "announcement", announcementService.toAnnouncementDTO(updated)
            )).build();

        } catch (IllegalArgumentException ex) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(java.util.Map.of(
                            "error", ex.getMessage()
                    ))
                    .build();
        }
    }


    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Long id,
                           @QueryParam("userId") Long userId,
                           HttpHeaders headers) {

        var announcementOpt = announcementService.findById(id);

        if (announcementOpt.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(java.util.Map.of(
                            "error", messageService.get("error.notfound", headers)
                    ))
                    .build();
        }

        Announcement announcement = announcementOpt.get();

        // Ownership check
        Response ownership = checkOwnership(userId, announcement, headers);
        if (ownership != null) return ownership;

        boolean deleted = announcementService.delete(id);

        if (!deleted) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(java.util.Map.of(
                            "error", messageService.get("error.notfound", headers)
                    ))
                    .build();
        }

        return Response.ok(java.util.Map.of(
                "message", messageService.get("announcement.deleted", headers)
        )).build();
    }

    // ------------------------------------------------------------
    // PHOTO UPLOAD
    // ------------------------------------------------------------

    @POST
    @Path("/{id}/photos")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadPhoto(@PathParam("id") Long id,
                                @QueryParam("userId") Long userId,
                                @FormParam("file") byte[] file,
                                HttpHeaders headers) {

        var announcementOpt = announcementService.findById(id);

        if (announcementOpt.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(java.util.Map.of(
                            "error", messageService.get("error.notfound", headers)
                    ))
                    .build();
        }

        Announcement announcement = announcementOpt.get();

        // Ownership check
        Response ownership = checkOwnership(userId, announcement, headers);
        if (ownership != null) return ownership;

        // Check limit
        if (!imageLimitService.canAddAnnouncementPhoto(announcement)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(java.util.Map.of(
                            "error", messageService.get("photo.limit", headers)
                    ))
                    .build();
        }

        // Upload to S3
        String url;
        try {
            url = imageStoreService.upload(file);
        } catch (IllegalArgumentException ex) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(java.util.Map.of(
                            "error", messageService.get(ex.getMessage(), headers)
                    ))
                    .build();
        }

        // Save photo in DB
        Photo photo = new Photo();
        photo.setUrl(url);
        photo.setAnnouncement(announcement);

        Photo saved = photoService.create(photo);

        return Response.ok(java.util.Map.of(
                "message", messageService.get("photo.uploaded", headers),
                "photo", photoService.toDTO(saved)
        )).build();
    }

    // ------------------------------------------------------------
    // PHOTO DELETE
    // ------------------------------------------------------------

    @DELETE
    @Path("/{announcementId}/photos/{photoId}")
    public Response deletePhoto(@PathParam("announcementId") Long announcementId,
                                @PathParam("photoId") Long photoId,
                                @QueryParam("userId") Long userId,
                                HttpHeaders headers) {

        // 1. Check announcement exists
        var announcementOpt = announcementService.findById(announcementId);
        if (announcementOpt.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(java.util.Map.of(
                            "error", messageService.get("error.notfound", headers)
                    ))
                    .build();
        }

        Announcement announcement = announcementOpt.get();

        // 2. Ownership check
        Response ownership = checkOwnership(userId, announcement, headers);
        if (ownership != null) return ownership;

        // 3. Check photo exists
        var photoOpt = photoService.findById(photoId);
        if (photoOpt.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(java.util.Map.of(
                            "error", messageService.get("error.notfound", headers)
                    ))
                    .build();
        }

        Photo photo = photoOpt.get();

        // 4. Ensure photo belongs to this announcement
        if (!photo.getAnnouncement().getId().equals(announcementId)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(java.util.Map.of(
                            "error", messageService.get("photo.mismatch", headers)
                    ))
                    .build();
        }

        // 5. Delete from S3
        imageStoreService.delete(photo.getUrl());

        // 6. Delete from DB
        photoService.delete(photoId);

        return Response.ok(java.util.Map.of(
                "message", messageService.get("photo.deleted", headers)
        )).build();
    }

    @PUT
    @Path("/{id}/photos/sort")
    public Response sortPhotos(@PathParam("id") Long id,
                               @QueryParam("userId") Long userId,
                               List<PhotoDTO> sortedPhotos,
                               HttpHeaders headers) {

        // 1. Check announcement exists
        var announcementOpt = announcementService.findById(id);
        if (announcementOpt.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(java.util.Map.of(
                            "error", messageService.get("error.notfound", headers)
                    ))
                    .build();
        }

        Announcement announcement = announcementOpt.get();

        // 2. Ownership check
        Response ownership = checkOwnership(userId, announcement, headers);
        if (ownership != null) return ownership;

        // 3. Validate all photos belong to this announcement
        for (PhotoDTO dto : sortedPhotos) {
            var photoOpt = photoService.findById(dto.id);

            if (photoOpt.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(java.util.Map.of(
                                "error", messageService.get("error.notfound", headers)
                        ))
                        .build();
            }

            Photo photo = photoOpt.get();

            if (!photo.getAnnouncement().getId().equals(id)) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(java.util.Map.of(
                                "error", messageService.get("photo.mismatch", headers)
                        ))
                        .build();
            }
        }

        // 4. Apply new positions
        for (int i = 0; i < sortedPhotos.size(); i++) {
            PhotoDTO dto = sortedPhotos.get(i);
            Photo photo = photoService.findById(dto.id).get();
            photo.setPosition(i);
            photoService.update(photo);
        }

        return Response.ok(java.util.Map.of(
                "message", messageService.get("photo.sorted", headers)
        )).build();
    }

    @PUT
    @Path("/{announcementId}/photos/{photoId}/main")
    public Response setMainPhoto(@PathParam("announcementId") Long announcementId,
                                 @PathParam("photoId") Long photoId,
                                 @QueryParam("userId") Long userId,
                                 HttpHeaders headers) {

        // 1. Check announcement exists
        var announcementOpt = announcementService.findById(announcementId);
        if (announcementOpt.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(java.util.Map.of(
                            "error", messageService.get("error.notfound", headers)
                    ))
                    .build();
        }

        Announcement announcement = announcementOpt.get();

        // 2. Ownership check
        Response ownership = checkOwnership(userId, announcement, headers);
        if (ownership != null) return ownership;

        // 3. Check photo exists
        var photoOpt = photoService.findById(photoId);
        if (photoOpt.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(java.util.Map.of(
                            "error", messageService.get("error.notfound", headers)
                    ))
                    .build();
        }

        Photo selected = photoOpt.get();

        // 4. Ensure photo belongs to this announcement
        if (!selected.getAnnouncement().getId().equals(announcementId)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(java.util.Map.of(
                            "error", messageService.get("photo.mismatch", headers)
                    ))
                    .build();
        }

        // 5. Reorder photos: selected becomes position 0
        List<Photo> photos = announcement.getPhotos();

        // Step 1: set selected photo to position 0
        selected.setPosition(0);
        photoService.update(selected);

        // Step 2: shift all other photos
        int pos = 1;
        for (Photo p : photos) {
            if (!p.getId().equals(photoId)) {
                p.setPosition(pos++);
                photoService.update(p);
            }
        }

        return Response.ok(java.util.Map.of(
                "message", messageService.get("photo.main.set", headers)
        )).build();
    }

    private Response checkOwnership(Long userId, Announcement announcement, HttpHeaders headers) {
        if (!announcementService.isOwner(userId, announcement)) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(java.util.Map.of(
                            "error", messageService.get("error.forbidden", headers)
                    ))
                    .build();
        }
        return null; // means OK
    }

    @POST
    @Path("/search")
    public PaginationResponse<AnnouncementDTO> search(AnnouncementSearchDTO filters,
                                                      @QueryParam("page") @DefaultValue("0") int page,
                                                      @QueryParam("size") @DefaultValue("10") int size) {

        return announcementService.search(filters, page, size);
    }
}
