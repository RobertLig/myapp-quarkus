package org.example.myapp.controller;

import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import java.util.Map;
import org.example.myapp.service.UserService;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
public class AuthController {

    @Inject
    UserService userService;

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
}
