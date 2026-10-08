package org.example.myapp.controller;

import io.quarkus.security.identity.SecurityIdentity;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.core.Context;
import org.example.myapp.dto.AnnouncementDTO;
import org.example.myapp.dto.PaginationResponse;
import org.example.myapp.dto.PhotoDTO;
import org.example.myapp.dto.AnnouncementSearchDTO;
import org.example.myapp.model.Announcement;
import org.example.myapp.model.Photo;
import org.example.myapp.service.AnnouncementService;
import org.example.myapp.service.PhotoService;
import org.example.myapp.service.AnnouncementPhotoService;
import org.example.myapp.service.ImageStoreService;
import org.example.myapp.service.ImageLimitService;
import org.example.myapp.i18n.MessageService;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.UriInfo;
import jakarta.validation.Valid;

import java.net.URI;
import java.util.List;
import java.util.Map;

@Path("/announcements")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AnnouncementController {

    @Inject
    AnnouncementService announcementService;

    @Inject
    PhotoService photoService;

    @Inject
    AnnouncementPhotoService announcementPhotoService;

    @Inject
    ImageStoreService imageStoreService;

    @Inject
    ImageLimitService imageLimitService;

    @Inject
    MessageService messageService;

    @Inject
    SecurityIdentity identity;

    private Long getLoggedInUserId() {
        return Long.valueOf(identity.getPrincipal().getName());
    }

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
    @RolesAllowed({"USER", "ADMIN"})
    public Response create(
            @Valid AnnouncementDTO dto,
            @Context HttpHeaders headers,
            @Context UriInfo uriInfo) {

        dto.userId = getLoggedInUserId();

        String defaultLang = resolveLanguageHeader(headers);
        AnnouncementDTO createdDto = announcementService.create(dto, defaultLang);

        // Build URI for the new resource: http://hostname/announcements/{id}
        URI locationUri = uriInfo.getAbsolutePathBuilder()
                .path(String.valueOf(createdDto.getId()))
                .build();

        return Response.created(locationUri)
                .entity(Map.of(
                        "message", messageService.get("announcement.created", headers),
                        "announcement", createdDto
                ))
                .build();
    }

    private String resolveLanguageHeader(HttpHeaders headers) {
        if (headers != null) {
            var locales = headers.getAcceptableLanguages();
            if (locales != null && !locales.isEmpty() && locales.get(0) != null) {
                String lang = locales.get(0).getLanguage();
                if (lang != null && !lang.isBlank()) {
                    return lang.toLowerCase().trim();
                }
            }
        }
        return "en";
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed({"USER", "ADMIN"})
    public Response update(@PathParam("id") Long id,
                           @Valid AnnouncementDTO dto,
                           @Context HttpHeaders headers) {

        Long loggedInUserId = getLoggedInUserId();
        String defaultLang = resolveLanguageHeader(headers);

        AnnouncementDTO updatedDto = announcementService.update(id, dto, loggedInUserId, defaultLang);

        return Response.ok(Map.of(
                "message", messageService.get("announcement.updated", headers),
                "announcement", updatedDto
        )).build();
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
    @RolesAllowed({"USER", "ADMIN"})
    public Response uploadPhoto(@PathParam("id") Long announcementId,
                                @RestForm("file") FileUpload fileUpload) {
        Long currentUserId = getLoggedInUserId();
        PhotoDTO photoDTO = announcementPhotoService.uploadPhoto(announcementId, currentUserId, fileUpload);
        return Response.status(Response.Status.CREATED).entity(photoDTO).build();
    }

    // ------------------------------------------------------------
    // PHOTO DELETE
    // ------------------------------------------------------------

    @DELETE
    @Path("/{id}/photos/{photoId}")
    @RolesAllowed({"USER", "ADMIN"})
    public Response deletePhoto(@PathParam("id") Long announcementId,
                                @PathParam("photoId") Long photoId) {
        Long currentUserId = getLoggedInUserId();
        announcementPhotoService.deletePhoto(announcementId, photoId, currentUserId);
        return Response.noContent().build();
    }

    @PUT
    @Path("/{id}/photos/sort")
    @RolesAllowed({"USER", "ADMIN"})
    public Response sortPhotos(@PathParam("id") Long announcementId,
                               List<PhotoDTO> sortedPhotos) {
        Long currentUserId = getLoggedInUserId();
        List<PhotoDTO> updatedPhotos = announcementPhotoService.sortPhotos(announcementId, currentUserId, sortedPhotos);
        return Response.ok(updatedPhotos).build();
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
