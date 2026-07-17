package org.example.myapp.dto;

import jakarta.validation.constraints.NotBlank;

public class AnnouncementTranslationDTO {

    @NotBlank(message = "Language code is required")
    public String language; // "en", "pl", etc.

    @NotBlank(message = "Title is required")
    public String title;

    @NotBlank(message = "Description is required")
    public String description;
}
