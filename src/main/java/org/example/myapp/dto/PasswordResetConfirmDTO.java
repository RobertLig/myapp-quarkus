package org.example.myapp.dto;

import jakarta.validation.constraints.NotBlank;

public class PasswordResetConfirmDTO {

    @NotBlank(message = "{validation.reset.token.required}")
    private String token;

    @NotBlank(message = "{validation.reset.password.required}")
    private String password;

    public String getToken() {
        return token;
    }

    public String getPassword() {
        return password;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
