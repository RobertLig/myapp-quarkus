package org.example.myapp.controller;

import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.*;
import org.example.myapp.repository.UserRepository;
import org.example.myapp.model.User;
import jakarta.ws.rs.core.Response;
import java.util.Map;
import org.example.myapp.service.EmailService;
import org.example.myapp.service.TokenService;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
public class AuthController {

    @Inject
    UserRepository userRepository;

    @Inject
    EmailService emailService;

    @Inject
    TokenService tokenService;

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

    @POST
    @Path("/reset/request")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response requestReset(Map<String, String> body) {

        String email = body.get("email");
        User user = userRepository.find("email", email).firstResult();

        if (user == null) {
            throw new WebApplicationException("error.email.notfound", 404);
        }

        String token = tokenService.generateToken();
        user.setResetPasswordToken(token);

        emailService.sendActionEmail(
                user.getEmail(),
                user.getLocale(),
                "email.reset.subject",
                "email.reset.intro",
                "email.reset.button",
                "email.reset.fallback",
                "https://yourdomain.com/auth/reset?token=" + token
        );


        return Response.ok(Map.of("message", "success.reset.email.sent")).build();
    }

    @POST
    @Path("/reset/confirm")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response confirmReset(Map<String, String> body) {

        String token = body.get("token");
        String newPassword = body.get("password");

        User user = userRepository.find("resetPasswordToken", token).firstResult();

        if (user == null) {
            throw new WebApplicationException("error.reset.invalid", 400);
        }

        // Hash new password
        String salt = generateSalt();
        user.setSalt(salt);
        user.setPassword(hashPassword(newPassword, salt));

        // Invalidate token
        user.setResetPasswordToken(null);

        return Response.ok(Map.of("message", "success.reset.completed")).build();
    }
}
