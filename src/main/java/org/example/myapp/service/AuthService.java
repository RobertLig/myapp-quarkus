package org.example.myapp.service;

import io.smallrye.jwt.build.Jwt;
import io.smallrye.jwt.auth.principal.JWTParser;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.example.myapp.exception.DomainException;
import org.example.myapp.exception.EntityNotFoundException;
import org.example.myapp.model.User;
import java.util.Collections;

@ApplicationScoped
public class AuthService {

    @Inject
    UserService userService;

    @Inject
    JWTParser jwtParser;

    public String generateToken(User user) {
        return Jwt.issuer("your-app")
                .subject(String.valueOf(user.getId()))
                .groups(Collections.singleton(user.getRole().name()))
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
                    .orElseThrow(() -> new EntityNotFoundException("error.user.notfound"));

            return generateToken(user);

        } catch (Exception e) {
            // Refresh token failure (401 Unauthorized)
            throw DomainException.unauthorized("error.token.invalid");
        }
    }
}

