package org.example.myapp.dto;

import java.time.LocalDateTime;
import java.util.List;
import jakarta.validation.constraints.*;
import jakarta.validation.Valid;

public class AnnouncementDTO {

    public Long id;

    @NotBlank(message = "Type is required")
    public String type; // "sender" or "courier"

    @NotNull(message = "User ID is required")
    public Long userId;

    @Valid
    @NotNull(message = "Dimensions are required")
    public AnnouncementDimensionsDTO dimensions;

    @Valid
    @NotNull(message = "Weight is required")
    public AnnouncementWeightDTO weight;

    @NotBlank(message = "Posting place is required")
    public String postingPlace;

    @NotBlank(message = "Reception place is required")
    public String receptionPlace;

    @NotNull(message = "Posting date/time is required")
    public LocalDateTime postingDateTime;

    @NotNull(message = "Reception date/time is required")
    public LocalDateTime receptionDateTime;

    @Valid
    @Size(min = 1, message = "At least one translation is required")
    public List<AnnouncementTranslationDTO> translations;

    @Valid
    @Size(min = 0, message = "Stops list cannot be null")
    public List<StopDTO> stops;

    @Valid
    @Size(min = 0, message = "Photos list cannot be null")
    public List<PhotoDTO> photos;
}



