package org.example.myapp.controller;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.HttpHeaders;

import org.example.myapp.dto.UserDTO;
import org.example.myapp.model.User;
import org.example.myapp.service.UserService;
import org.example.myapp.service.ImageStoreService;
import org.example.myapp.service.ImageLimitService;
import org.example.myapp.i18n.MessageService;
import java.util.Optional;

@Path("/users")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class UserController {

    @Inject
    UserService userService;

    @Inject
    ImageStoreService imageStoreService;

    @Inject
    ImageLimitService imageLimitService;

    @Inject
    MessageService messageService;

    // ===== REGISTER =====
    @POST
    @Path("/register")
    public Response register(UserDTO dto) {
        try {
            User user = userService.register(dto);
            return Response.ok(user).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(e.getMessage())
                    .build();
        }
    }

    // ===== LOGIN =====
    @POST
    @Path("/login")
    public Response login(UserDTO dto) {
        try {
            User user = userService.login(dto.getEmail(), dto.getPassword());
            return Response.ok(user).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(e.getMessage())
                    .build();
        }
    }

    // ===== UPDATE PROFILE =====
    @PUT
    @Path("/{id}")
    public Response updateProfile(@PathParam("id") Long id, UserDTO dto) {
        try {
            User updated = userService.updateProfile(id, dto);
            return Response.ok(updated).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(e.getMessage())
                    .build();
        }
    }

    // ===== DELETE USER =====
    @DELETE
    @Path("/{id}")
    public Response deleteUser(@PathParam("id") Long id) {
        Optional<User> userOpt = userService.getUserById(id);

        if (userOpt.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("User not found")
                    .build();
        }

        User user = userOpt.get();


        // Delete avatar from S3 if exists
        if (user.getPhotoUrl() != null) {
            imageStoreService.delete(user.getPhotoUrl());
        }

        boolean deleted = userService.deleteUser(id);

        return deleted
                ? Response.ok("User deleted").build()
                : Response.status(Response.Status.NOT_FOUND).build();
    }

    // ===== GET USER BY ID =====
    @GET
    @Path("/{id}")
    public Response getUser(@PathParam("id") Long id) {
        Optional<User> userOpt = userService.getUserById(id);

        if (userOpt.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("User not found")
                    .build();
        }

        User user = userOpt.get();
        return Response.ok(user).build();
    }

    // ============================================================
    // =============== USER AVATAR UPLOAD ==========================
    // ============================================================

    @POST
    @Path("/{id}/avatar")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadAvatar(@PathParam("id") Long id,
                                 @FormParam("file") byte[] file,
                                 HttpHeaders headers) {

        Optional<User> userOpt = userService.getUserById(id);

        if (userOpt.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(java.util.Map.of(
                            "error", messageService.get("error.notfound", headers)
                    ))
                    .build();
        }

        User user = userOpt.get();

        // Limit: user can have only 1 avatar
        if (!imageLimitService.canAddUserAvatar(user)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(java.util.Map.of(
                            "error", messageService.get("avatar.limit", headers)
                    ))
                    .build();
        }

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

        // Save avatar URL
        user.setPhotoUrl(url);

        return Response.ok(java.util.Map.of(
                "message", messageService.get("avatar.uploaded", headers),
                "avatarUrl", url
        )).build();
    }

    // ============================================================
    // =============== USER AVATAR DELETE ==========================
    // ============================================================

    @DELETE
    @Path("/{id}/avatar")
    public Response deleteAvatar(@PathParam("id") Long id,
                                 HttpHeaders headers) {

        Optional<User> userOpt = userService.getUserById(id);

        if (userOpt.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(java.util.Map.of(
                            "error", messageService.get("error.notfound", headers)
                    ))
                    .build();
        }

        User user = userOpt.get();

        if (user.getPhotoUrl() == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(java.util.Map.of(
                            "error", messageService.get("avatar.none", headers)
                    ))
                    .build();
        }

        // Delete from S3
        imageStoreService.delete(user.getPhotoUrl());

        // Remove from DB
        user.setPhotoUrl(null);

        return Response.ok(java.util.Map.of(
                "message", messageService.get("avatar.deleted", headers)
        )).build();
    }
}
