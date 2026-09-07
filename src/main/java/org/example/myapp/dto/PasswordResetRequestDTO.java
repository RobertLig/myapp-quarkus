package org.example.myapp.dto;

public class PasswordResetRequestDTO {

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
