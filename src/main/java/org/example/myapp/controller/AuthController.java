package org.example.myapp.controller;

import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.*;
import org.example.myapp.repository.UserRepository;
import org.example.myapp.model.User;
import jakarta.ws.rs.core.Response;
import java.util.Map;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
public class AuthController {

    @Inject
    UserRepository userRepository;

    @GET
    @Path("/verify")
    public Response verifyEmail(@QueryParam("token") String token) {

        User user = userRepository.find("verificationToken", token).firstResult();

        if (user == null) {
            throw new WebApplicationException("error.verification.invalid", 400);
        }

        user.setEmailVerified(true);
        user.setVerificationToken(null);

        return Response.ok(
                Map.of("message", "success.email.verified")
        ).build();
    }
}
