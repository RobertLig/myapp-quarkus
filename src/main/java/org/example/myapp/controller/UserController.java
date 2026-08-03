package org.example.myapp.controller;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.HttpHeaders;

import org.example.myapp.dto.UserDTO;
import org.example.myapp.model.User;
import org.example.myapp.service.AuthService;
import org.example.myapp.service.UserService;
import org.example.myapp.service.ImageStoreService;
import org.example.myapp.service.ImageLimitService;
import org.example.myapp.i18n.MessageService;

import java.util.Map;

import io.quarkus.security.identity.SecurityIdentity;

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

    @Inject
    AuthService authService;

    @Inject
    SecurityIdentity identity;

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
        User user = userService.login(dto.getEmail(), dto.getPassword());
        String token = authService.generateToken(user);

        return Response.ok(
                Map.of(
                        "token", token,
                        "user", Map.of(
                                "id", user.getId(),
                                "name", user.getName(),
                                "email", user.getEmail(),
                                "photoUrl", user.getPhotoUrl()
                        )
                )
        ).build();
    }

    // ===== GET USER BY ID =====
    @GET
    @Path("/me")
    @RolesAllowed({"USER", "ADMIN"})
    public Response getMe() {
        Long userId = getLoggedInUserId();

        User user = userService.getUserById(userId)
                .orElseThrow(() -> new WebApplicationException("error.user.notfound", 404));

        return Response.ok(
                Map.of(
                        "id", user.getId(),
                        "name", user.getName(),
                        "email", user.getEmail(),
                        "photoUrl", user.getPhotoUrl()
                )
        ).build();
    }

    // ===== UPDATE PROFILE =====
    @PUT
    @Path("/me")
    @RolesAllowed({"USER", "ADMIN"})
    public Response updateMe(UserDTO dto) {
        Long userId = getLoggedInUserId();

        User updated = userService.updateProfile(userId, dto);

        return Response.ok(
                Map.of(
                        "id", updated.getId(),
                        "name", updated.getName(),
                        "email", updated.getEmail(),
                        "photoUrl", updated.getPhotoUrl()
                )
        ).build();
    }

    // ===== DELETE USER =====
    @DELETE
    @Path("/me")
    @RolesAllowed({"USER", "ADMIN"})
    public Response deleteMe() {
        Long userId = getLoggedInUserId();

        User user = userService.getUserById(userId)
                .orElseThrow(() -> new WebApplicationException("error.user.notfound", 404));

        if (user.getPhotoUrl() != null) {
            imageStoreService.delete(user.getPhotoUrl());
        }

        userService.deleteUser(userId);

        return Response.ok(
                Map.of("message", "success.user.deleted")
        ).build();
    }

    // ============================================================
    // =============== USER AVATAR UPLOAD ==========================
    // ============================================================

    @POST
    @Path("/me/avatar")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @RolesAllowed({"USER", "ADMIN"})
    public Response uploadAvatar(@FormParam("file") byte[] file, HttpHeaders headers) {

        Long userId = getLoggedInUserId();
        User user = userService.getUserById(userId)
                .orElseThrow(() -> new WebApplicationException("error.user.notfound", 404));

        if (!imageLimitService.canAddUserAvatar(user)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", messageService.get("avatar.limit", headers)))
                    .build();
        }

        String url = imageStoreService.upload(file);
        user.setPhotoUrl(url);

        return Response.ok(
                Map.of(
                        "message", messageService.get("avatar.uploaded", headers),
                        "avatarUrl", url
                )
        ).build();
    }

    // ============================================================
    // =============== USER AVATAR DELETE ==========================
    // ============================================================

    @DELETE
    @Path("/me/avatar")
    @RolesAllowed({"USER", "ADMIN"})
    public Response deleteAvatar(HttpHeaders headers) {

        Long userId = getLoggedInUserId();
        User user = userService.getUserById(userId)
                .orElseThrow(() -> new WebApplicationException("error.user.notfound", 404));

        if (user.getPhotoUrl() == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", messageService.get("avatar.none", headers)))
                    .build();
        }

        imageStoreService.delete(user.getPhotoUrl());
        user.setPhotoUrl(null);

        return Response.ok(
                Map.of("message", messageService.get("avatar.deleted", headers))
        ).build();
    }

    private Long getLoggedInUserId() {
        return Long.valueOf(identity.getPrincipal().getName());
    }
}
