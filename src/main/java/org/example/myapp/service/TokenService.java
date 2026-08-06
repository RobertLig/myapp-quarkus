package org.example.myapp.service;

import jakarta.enterprise.context.ApplicationScoped;

import java.security.SecureRandom;
import java.util.Base64;

@ApplicationScoped
public class TokenService {

    public String generateToken() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
