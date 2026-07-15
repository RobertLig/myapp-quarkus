package org.example.myapp.dto;

import java.time.LocalDateTime;
import java.util.List;
import jakarta.validation.constraints.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;


public class AnnouncementDTO {

    public Long id;

    @NotBlank(message = "Title is required")
    public String title;

    @NotBlank(message = "Description is required")
    public String description;

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
    @NotNull(message = "Sender is required")
    public SenderDTO sender;

    @Valid
    @NotNull(message = "Courier is required")
    public CourierDTO courier;

    @Size(min = 0, message = "Stops list cannot be null")
    public List<@Valid StopDTO> stops;

    @Size(min = 0, message = "Photos list cannot be null")
    public List<@Valid PhotoDTO> photos;
}


