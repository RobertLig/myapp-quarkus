package org.example.myapp.service;

import io.smallrye.jwt.build.Jwt;
import io.smallrye.jwt.auth.principal.JWTParser;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.example.myapp.model.User;

@ApplicationScoped
public class AuthService {

    @Inject
    UserService userService;

    @Inject
    JWTParser jwtParser;

    public String generateToken(User user) {
        return Jwt.issuer("your-app")
                .subject(String.valueOf(user.getId()))
                .expiresIn(900)
                .sign();
    } 

    public String generateRefreshToken(User user) {
        return Jwt.issuer("your-app")
                .subject(String.valueOf(user.getId()))
                .expiresIn(60 * 60 * 24 * 30)
                .sign();
    }

    public String refresh(String refreshToken) {
        try {
            var claims = jwtParser.parse(refreshToken);

            Long userId = Long.valueOf(claims.getSubject());
            User user = userService.getUserById(userId)
                    .orElseThrow(() -> new jakarta.ws.rs.WebApplicationException("error.user.notfound", 404));

            return generateToken(user);

        } catch (Exception e) {
            throw new jakarta.ws.rs.WebApplicationException("error.token.invalid", 401);
        }
    }
}

