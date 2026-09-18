package org.example.myapp.dto;

import jakarta.validation.constraints.NotBlank;

public class GoogleLoginDTO {

    @NotBlank(message = "{validation.google.idtoken.required}")
    private String idToken;

    public String getIdToken() {
        return idToken;
    }

    public void setIdToken(String idToken) {
        this.idToken = idToken;
    }
}
