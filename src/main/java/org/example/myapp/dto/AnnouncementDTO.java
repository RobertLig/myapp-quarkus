package org.example.myapp.dto;

import java.time.LocalDateTime;
import java.util.List;
import jakarta.validation.Valid;

public class AnnouncementDTO {

    public Long id;

    // Required
    public String type; // "sender" or "courier"

    // Required
    public Long userId;

    // Optional
    @Valid
    public AnnouncementDimensionsDTO dimensions;

    // Optional
    @Valid
    public AnnouncementWeightDTO weight;

    // Required
    public String postingPlace;

    // Required
    public String receptionPlace;

    // Required
    public LocalDateTime postingDateTime;

    // Required
    public LocalDateTime receptionDateTime;

    // Required: must contain exactly "en" and "pl"
    public List<@Valid AnnouncementTranslationDTO> translations;

    // Optional
    public List<@Valid StopDTO> stops;

    // Optional
    public List<@Valid PhotoDTO> photos;
}
