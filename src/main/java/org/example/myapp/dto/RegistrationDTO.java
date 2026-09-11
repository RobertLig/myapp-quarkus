package org.example.myapp.dto;

import jakarta.validation.constraints.*;

public class RegistrationDTO {

    @NotBlank(message = "{validation.user.name.required}")
    private String name;

    @Email(message = "{validation.user.email.invalid}")
    @NotBlank(message = "{validation.user.email.required}")
    private String email;

    @NotBlank(message = "{validation.user.password.required}")
    private String password;

    @NotNull(message = "{validation.user.terms.required}")
    private Boolean termsAccepted;

    // Honeypot field — bots fill this, humans never do
    private String trap;

    // Locale is set automatically by frontend (navigator.language)
    private String locale;

    // ===== GETTERS =====

    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public Boolean getTermsAccepted() { return termsAccepted; }
    public String getTrap() { return trap; }
    public String getLocale() { return locale; }

    // ===== SETTERS =====

    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public void setTermsAccepted(Boolean termsAccepted) { this.termsAccepted = termsAccepted; }
    public void setTrap(String trap) { this.trap = trap; }
    public void setLocale(String locale) { this.locale = locale; }
}