package org.example.myapp.controller;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.example.myapp.exception.EntityNotFoundException;
import org.example.myapp.i18n.MessageService;

import org.example.myapp.dto.ImagePresignRequestDTO;
import org.example.myapp.dto.ImageUploadCompleteRequestDTO;
import org.example.myapp.dto.UserDTO;
import org.example.myapp.mapper.UserMapper;
import org.example.myapp.model.User;
import org.example.myapp.service.*;

import java.util.Map;

import io.quarkus.security.identity.SecurityIdentity;

@Path("/users")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class UserController {

    @Inject
    UserService userService;

    @Inject
    UserAvatarService avatarService;

    @Inject
    SecurityIdentity identity;

    @Inject
    MessageService messageService;

    private Long getLoggedInUserId() {
        return Long.valueOf(identity.getPrincipal().getName());
    }

    @GET
    @Path("/me")
    @RolesAllowed({"USER", "ADMIN"})
    public Response getMe() {
        Long userId = getLoggedInUserId();
        User user = userService.getUserById(userId)
                .orElseThrow(() -> new EntityNotFoundException("error.user.notfound"));

        return Response.ok(UserMapper.toProfileDTO(user)).build();
    }

    @PUT
    @Path("/me")
    @RolesAllowed({"USER", "ADMIN"})
    public Response updateMe(@Valid UserDTO dto) {
        Long userId = getLoggedInUserId();
        User updated = userService.updateProfile(userId, dto);
        return Response.ok(UserMapper.toProfileDTO(updated)).build();
    }

    @DELETE
    @Path("/me")
    @RolesAllowed({"USER", "ADMIN"})
    public Response deleteMe(@Context HttpHeaders headers) {
        Long userId = getLoggedInUserId();

        userService.deleteUser(userId);

        return Response.ok(Map.of(
                "message", messageService.get("success.user.deleted", headers)
        )).build();
    }

    @POST
    @Path("/me/avatar/presign")
    @RolesAllowed({"USER", "ADMIN"})
    public Response presignAvatar(@Valid ImagePresignRequestDTO request) {
        Long userId = getLoggedInUserId();
        return Response.ok(avatarService.presignAvatar(userId, request.contentType, request.fileSize)).build();
    }

    @POST
    @Path("/me/avatar")
    @RolesAllowed({"USER", "ADMIN"})
    public Response completeAvatarUpload(@Valid ImageUploadCompleteRequestDTO request) {
        Long userId = getLoggedInUserId();
        String url = avatarService.completeAvatarUpload(userId, request);
        return Response.ok(Map.of("avatarUrl", url)).build();
    }

    @DELETE
    @Path("/me/avatar")
    @RolesAllowed({"USER", "ADMIN"})
    public Response deleteAvatar(@Context HttpHeaders headers) {
        Long userId = getLoggedInUserId();
        avatarService.deleteAvatar(userId);
        return Response.ok(Map.of(
                "message", messageService.get("avatar.deleted", headers)
        )).build();
    }
}
