package org.example.myapp.controller;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.core.*;
import jakarta.ws.rs.*;
import jakarta.annotation.PostConstruct;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.example.myapp.dto.*;
import org.example.myapp.i18n.MessageService;
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

    @Inject
    MessageService messageService;

    @Context
    io.vertx.core.http.HttpServerRequest request;

    @ConfigProperty(name = "rate-limit.trusted-proxies")
    Optional<String> trustedProxyConfig;

    private Set<InetAddress> trustedProxyAddresses = Set.of();

    @PostConstruct
    void configureTrustedProxies() {
        if (trustedProxyConfig.isEmpty() || trustedProxyConfig.get().isBlank()) {
            return;
        }

        trustedProxyAddresses = Arrays.stream(trustedProxyConfig.get().split(","))
                .map(String::trim)
                .map(address -> parseIpAddress(address)
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Invalid IP address in rate-limit.trusted-proxies: " + address)))
                .collect(Collectors.toUnmodifiableSet());
    }

    @GET
    @Path("/verify")
    public Response verifyEmail(@QueryParam("token") String token, @Context HttpHeaders headers) {
        userService.verifyEmail(token);
        return Response.ok(Map.of("message", messageService.get("success.email.verified", headers) )).build();
    }

    @POST
    @Path("/reset/request")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response requestReset(@Valid PasswordResetRequestDTO dto, @Context HttpHeaders headers) {

        String ip = getClientIp();
        userService.requestPasswordReset(dto.getEmail(), dto.getTrap(), ip);

        return Response.ok(Map.of("message", messageService.get("success.reset.email.sent", headers) )).build();
    }

    @POST
    @Path("/reset/confirm")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response confirmReset(@Valid PasswordResetConfirmDTO dto, @Context HttpHeaders headers) {

        userService.resetPassword(dto.getToken(), dto.getPassword());

        return Response.ok(Map.of("message", messageService.get("success.reset.completed", headers) )).build();
    }

    @POST
    @Path("/google")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response googleLogin(@Valid GoogleLoginDTO dto) {
        User user = userService.loginWithGoogle(dto.getIdToken());
        return buildAuthResponse(user);
    }

    @POST
    @Path("/login")
    public Response login(@Valid LoginDTO dto) {
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
    public Response register(@Valid RegistrationDTO dto, @Context HttpHeaders headers) {
        String ip = getClientIp();

        User user = userService.register(dto, ip);

        if (user.getId() == -1L) {
            // honeypot triggered → pretend success
            return Response.ok(Map.of("message", messageService.get("success.registration", headers))).build();
        }else {
            return Response.ok(UserMapper.toDTO(user)).build();
        }
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
                .sameSite(NewCookie.SameSite.LAX)
                .build();

        return Response.ok(
                Map.of("message", messageService.get("success.logout", headers) )
        ).cookie(clearedCookie).build();
    }

    private String getClientIp() {
        String remoteIp = request.remoteAddress().host();
        if (!isTrustedProxy(remoteIp)) {
            return remoteIp;
        }

        String xff = request.getHeader("X-Forwarded-For");
        if (xff == null || xff.isBlank()) {
            return remoteIp;
        }

        String[] forwardedAddresses = xff.split(",");
        for (int i = forwardedAddresses.length - 1; i >= 0; i--) {
            String address = forwardedAddresses[i].trim();
            InetAddress parsedAddress = parseIpAddress(address).orElse(null);
            if (parsedAddress == null) {
                return remoteIp;
            }
            if (!trustedProxyAddresses.contains(parsedAddress)) {
                return parsedAddress.getHostAddress();
            }
        }
        return remoteIp;
    }

    private boolean isTrustedProxy(String address) {
        return parseIpAddress(address).filter(trustedProxyAddresses::contains).isPresent();
    }

    private Optional<InetAddress> parseIpAddress(String address) {
        if (address.isBlank() || !address.matches("[0-9a-fA-F:.]+")) {
            return java.util.Optional.empty();
        }
        try {
            return java.util.Optional.of(InetAddress.getByName(address));
        } catch (UnknownHostException e) {
            return java.util.Optional.empty();
        }
    }
}
