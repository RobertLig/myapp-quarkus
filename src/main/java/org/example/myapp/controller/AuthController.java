package org.example.myapp.controller;

import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import java.util.Map;

import org.example.myapp.dto.UserDTO;
import org.example.myapp.mapper.UserMapper;
import org.example.myapp.model.User;
import org.example.myapp.service.UserService;
import org.example.myapp.service.AuthService;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
public class AuthController {

    @Inject
    UserService userService;

    @Inject AuthService authService;

    @GET
    @Path("/verify")
    public Response verifyEmail(@QueryParam("token") String token) {
        userService.verifyEmail(token);
        return Response.ok(Map.of("message", "success.email.verified")).build();
    }

    @POST
    @Path("/reset/request")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response requestReset(Map<String, String> body) {
        String email = body.get("email");
        userService.requestPasswordReset(email);
        return Response.ok(Map.of("message", "success.reset.email.sent")).build();
    }

    @POST
    @Path("/reset/confirm")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response confirmReset(Map<String, String> body) {

        String token = body.get("token");
        String newPassword = body.get("password");

        userService.resetPassword(token, newPassword);

        return Response.ok(Map.of("message", "success.reset.completed")).build();
    }

    @POST
    @Path("/google")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response googleLogin(Map<String, String> body) {
        String idToken = body.get("idToken");

        User user = userService.loginWithGoogle(idToken);
        String token = authService.generateToken(user);

        return Response.ok(
                Map.of(
                        "token", token,
                        "user", UserMapper.toDTO(user)
                )
        ).build();
    }

    @POST
    @Path("/login")
    public Response login(UserDTO dto) {
        User user = userService.login(dto.getEmail(), dto.getPassword());
        String token = authService.generateToken(user);

        return Response.ok(
                Map.of(
                        "token", token,
                        "user", UserMapper.toDTO(user)
                )
        ).build();
    }
}
