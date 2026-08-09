package org.example.myapp.controller;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

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

    @Inject UserService userService;
    @Inject
    UserAvatarService avatarService;
    @Inject SecurityIdentity identity;

    private Long getLoggedInUserId() {
        return Long.valueOf(identity.getPrincipal().getName());
    }

    @POST
    @Path("/register")
    public Response register(UserDTO dto) {
        User user = userService.register(dto);
        return Response.ok(UserMapper.toDTO(user)).build();
    }

    @GET
    @Path("/me")
    @RolesAllowed({"USER", "ADMIN"})
    public Response getMe() {
        Long userId = getLoggedInUserId();
        User user = userService.getUserById(userId)
                .orElseThrow(() -> new WebApplicationException("error.user.notfound", 404));

        return Response.ok(UserMapper.toDTO(user)).build();
    }

    @PUT
    @Path("/me")
    @RolesAllowed({"USER", "ADMIN"})
    public Response updateMe(UserDTO dto) {
        Long userId = getLoggedInUserId();
        User updated = userService.updateProfile(userId, dto);
        return Response.ok(UserMapper.toDTO(updated)).build();
    }

    @DELETE
    @Path("/me")
    @RolesAllowed({"USER", "ADMIN"})
    public Response deleteMe() {
        Long userId = getLoggedInUserId();
        userService.deleteUser(userId);
        return Response.ok(Map.of("message", "success.user.deleted")).build();
    }

    @POST
    @Path("/me/avatar")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @RolesAllowed({"USER", "ADMIN"})
    public Response uploadAvatar(@FormParam("file") byte[] file) {
        Long userId = getLoggedInUserId();
        String url = avatarService.uploadAvatar(userId, file);
        return Response.ok(Map.of("avatarUrl", url)).build();
    }

    @DELETE
    @Path("/me/avatar")
    @RolesAllowed({"USER", "ADMIN"})
    public Response deleteAvatar() {
        Long userId = getLoggedInUserId();
        avatarService.deleteAvatar(userId);
        return Response.ok(Map.of("message", "avatar.deleted")).build();
    }
}

