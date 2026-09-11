package org.example.myapp.controller;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import java.util.Map;

import org.example.myapp.dto.PasswordResetRequestDTO;
import org.example.myapp.dto.RegistrationDTO;
import org.example.myapp.dto.UserDTO;
import org.example.myapp.mapper.UserMapper;
import org.example.myapp.model.User;
import org.example.myapp.service.UserService;
import org.example.myapp.service.AuthService;
import jakarta.ws.rs.core.NewCookie;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
public class AuthController {

    @Inject
    UserService userService;

    @Inject AuthService authService;

    @Context
    io.vertx.core.http.HttpServerRequest request;

    @GET
    @Path("/verify")
    public Response verifyEmail(@QueryParam("token") String token) {
        userService.verifyEmail(token);
        return Response.ok(Map.of("message", "success.email.verified")).build();
    }

    @POST
    @Path("/reset/request")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response requestReset(PasswordResetRequestDTO dto) {

        String ip = getClientIp();
        userService.requestPasswordReset(dto.getEmail(), dto.getTrap(), ip);

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
        return buildAuthResponse(user);
    }

    @POST
    @Path("/login")
    public Response login(UserDTO dto) {
        User user = userService.login(dto.getEmail(), dto.getPassword());
        return buildAuthResponse(user);
    }

    private Response buildAuthResponse(User user) {

        String accessToken = authService.generateToken(user);
        String refreshToken = authService.generateRefreshToken(user);

        NewCookie cookie = new NewCookie.Builder("refreshToken")
                .value(refreshToken)
                .path("/auth/refresh")
                .maxAge(60 * 60 * 24 * 30)   // 30 days
                .httpOnly(true)
                .secure(true)
                .sameSite(NewCookie.SameSite.LAX)
                .build();

        return Response.ok(
                Map.of(
                        "token", accessToken,
                        "user", UserMapper.toDTO(user)
                )
        ).cookie(cookie).build();
    }

    @POST
    @Path("/register")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response register(@Valid RegistrationDTO dto) {
        String ip = getClientIp();

        User user = userService.register(dto, ip);

        if (user.getId() == -1L) {
            // honeypot triggered → pretend success
            return Response.ok(Map.of("message", "success.registration")).build();
        }else {
            return Response.ok(UserMapper.toDTO(user)).build();
        }
    }

    @POST
    @Path("/refresh")
    public Response refresh(@CookieParam("refreshToken") String refreshToken) {

        String newAccessToken = authService.refresh(refreshToken);

        return Response.ok(
                Map.of("token", newAccessToken)
        ).build();
    }

    @POST
    @Path("/logout")
    public Response logout() {

        NewCookie clearedCookie = new NewCookie.Builder("refreshToken")
                .value("")
                .path("/auth/refresh")
                .maxAge(0)                     // expire immediately
                .httpOnly(true)
                .secure(true)
                .sameSite(NewCookie.SameSite.LAX)
                .build();

        return Response.ok(
                Map.of("message", "success.logout")
        ).cookie(clearedCookie).build();
    }

    private String getClientIp() {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return request.remoteAddress().host();
    }
}
