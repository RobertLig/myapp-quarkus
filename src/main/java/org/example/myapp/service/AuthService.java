package org.example.myapp.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.example.myapp.model.User;

import java.util.Date;
import io.jsonwebtoken.SignatureAlgorithm;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.jsonwebtoken.security.Keys;
import java.security.Key;

@ApplicationScoped
public class AuthService {

    @ConfigProperty(name = "jwt.secret")
    String secretKey;

    @Inject
    UserService userService;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    public String generateToken(User user) {
        return Jwts.builder()
                .setSubject(String.valueOf(user.getId()))
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000L * 60 * 15)) // 15 min
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(User user) {
        return Jwts.builder()
                .setSubject(String.valueOf(user.getId()))
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 30)) // 30 days
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String refresh(String refreshToken) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(refreshToken)
                    .getBody();

            Long userId = Long.valueOf(claims.getSubject());
            User user = userService.getUserById(userId)
                    .orElseThrow(() -> new jakarta.ws.rs.WebApplicationException("error.user.notfound", 404));

            return generateToken(user);

        } catch (Exception e) {
            throw new jakarta.ws.rs.WebApplicationException("error.token.invalid", 401);
        }
    }

    public Long validateAndExtractUserId(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            return Long.valueOf(claims.getSubject());

        } catch (Exception e) {
            throw new jakarta.ws.rs.WebApplicationException("error.token.invalid", 401);
        }
    }
}
