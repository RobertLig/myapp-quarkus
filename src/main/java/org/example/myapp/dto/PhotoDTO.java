package org.example.myapp.dto;

import jakarta.validation.constraints.*;

public class PhotoDTO {

    public Long id;

    @NotBlank(message = "Photo URL is required")
    public String url;
}


