package org.example.myapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class AnnouncementTranslationDTO {

    @NotBlank(message = "{error.translation.language.required}")
    @Pattern(regexp = "^(en|pl)$", message = "{error.translation.language.invalid}")
    public String language; // "en", "pl", etc.

    @NotBlank(message = "{error.translation.title.required}")
    public String title;

    public String description;
}
