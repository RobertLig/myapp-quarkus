package org.example.myapp.service;

import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import org.example.myapp.model.User;

import java.time.Duration;

@ApplicationScoped
public class AuthService {

    public String generateToken(User user) {
        return Jwt.issuer("myapp")
                .subject(String.valueOf(user.getId()))
                .upn(user.getEmail())
                .groups(user.getRole().name())
                .claim("userId", user.getId())
                .claim("name", user.getName())
                .expiresIn(Duration.ofHours(2))
                .sign();
    }
}
