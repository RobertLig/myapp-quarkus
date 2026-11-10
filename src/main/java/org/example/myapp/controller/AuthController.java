package org.example.myapp.controller;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.core.*;
import jakarta.ws.rs.*;

import java.util.Map;

import org.example.myapp.dto.GoogleLoginDTO;
import org.example.myapp.i18n.MessageService;
import org.example.myapp.mapper.UserMapper;
import org.example.myapp.model.Locale;
import org.example.myapp.model.User;
import org.example.myapp.service.UserService;
import org.example.myapp.service.AuthService;
import org.example.myapp.service.PrivateObjectUrlService;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
public class AuthController {

    @Inject
    UserService userService;

    @Inject AuthService authService;

    @Inject
    MessageService messageService;

    @Inject
    PrivateObjectUrlService privateObjectUrlService;

    @POST
    @Path("/google")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response googleLogin(@Valid GoogleLoginDTO dto, @Context HttpHeaders headers) {
        User user = userService.loginWithGoogle(dto.getIdToken(), resolveUserLocale(headers));
        return buildAuthResponse(user);
    }

    private Locale resolveUserLocale(HttpHeaders headers) {
        var languages = headers == null ? null : headers.getAcceptableLanguages();
        if (languages != null && !languages.isEmpty()
                && "pl".equalsIgnoreCase(languages.get(0).getLanguage())) {
            return Locale.pl;
        }
        return Locale.en;
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
                .sameSite(NewCookie.SameSite.NONE)
                .build();

        return Response.ok(
                Map.of(
                        "token", accessToken,
                        "user", UserMapper.toDTO(user, privateObjectUrlService::readUrl)
                )
        ).cookie(cookie).build();
    }

    @POST
    @Path("/refresh")
    public Response refresh(@CookieParam("refreshToken") String refreshToken, @Context HttpHeaders headers) {

        if (refreshToken == null || refreshToken.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", messageService.get("missing.refresh.token.cookie", headers) )) //"Refresh token cookie is missing"
                    .build();
        }

        String newAccessToken = authService.refresh(refreshToken);

        return Response.ok(
                Map.of("token", newAccessToken)
        ).build();
    }

    @POST
    @Path("/logout")
    public Response logout(@Context HttpHeaders headers) {

        NewCookie clearedCookie = new NewCookie.Builder("refreshToken")
                .value("")
                .path("/auth/refresh")
                .maxAge(0)                     // expire immediately
                .httpOnly(true)
                .secure(true)
                .sameSite(NewCookie.SameSite.NONE)
                .build();

        return Response.ok(
                Map.of("message", messageService.get("success.logout", headers) )
        ).cookie(clearedCookie).build();
    }

}
