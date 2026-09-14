package org.example.myapp.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class PasswordResetRequestDTO {

    @Email(message = "{validation.user.email.invalid}")
    @NotBlank(message = "{validation.user.email.required}")
    private String email;

    // Honeypot field — bots fill this, humans never do
    private String trap;

    // ===== GETTERS =====

    public String getEmail() {
        return email;
    }

    public String getTrap() {
        return trap;
    }

    // ===== SETTERS =====

    public void setEmail(String email) {
        this.email = email;
    }

    public void setTrap(String trap) {
        this.trap = trap;
    }
}
