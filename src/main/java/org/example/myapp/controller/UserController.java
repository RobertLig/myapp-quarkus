package org.example.myapp.controller;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.example.myapp.dto.UserDTO;
import org.example.myapp.model.User;
import org.example.myapp.service.UserService;

@Path("/users")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class UserController {

    @Inject
    UserService userService;

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

    // ===== GET USER BY ID =====
    @GET
    @Path("/{id}")
    public Response getUser(@PathParam("id") Long id) {
        User user = userService.getUserById(id);

        if (user == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("User not found")
                    .build();
        }

        return Response.ok(user).build();
    }
}
