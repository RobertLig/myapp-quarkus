package org.example.myapp.service;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class EmailService {

    public void sendVerificationEmail(String email, String token) {

        String link = "https://yourdomain.com/auth/verify?token=" + token;

        // DEV MODE: print link instead of sending email
        System.out.println("Verification link: " + link);

        // TODO: integrate Mailgun / SendGrid / Resend API here
    }
}
