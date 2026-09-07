package org.example.myapp.service;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.Set;

@ApplicationScoped
public class DisposableEmailService {

    private static final Set<String> DISPOSABLE_DOMAINS = Set.of(
            "mailinator.com",
            "tempmail.com",
            "temp-mail.org",
            "10minutemail.com",
            "yopmail.com",
            "guerrillamail.com",
            "trashmail.com",
            "getnada.com",
            "dispostable.com"
    );

    public boolean isDisposable(String email) {
        if (email == null || !email.contains("@")) return false;
        String domain = email.substring(email.indexOf("@") + 1).toLowerCase();
        return DISPOSABLE_DOMAINS.contains(domain);
    }
}
