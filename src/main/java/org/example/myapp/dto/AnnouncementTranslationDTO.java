package org.example.myapp.dto;

import jakarta.validation.constraints.NotBlank;

public class AnnouncementTranslationDTO {

    public String language; // "en", "pl", etc.

    public String title;

    public String description;
}
