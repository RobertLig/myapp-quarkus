package org.example.myapp.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.Range;


import java.time.LocalDateTime;
import java.util.List;

public class AnnouncementDTO {

    public Long id;

    @NotBlank(message = "{error.type.required}")
    @Pattern(regexp = "^(sender|courier)$", message = "{error.type.invalid}")
    public String type; // "sender" or "courier"

    @NotNull(message = "{error.user.required}")
    public Long userId;

    // Dimensions (DTO includes unit for input/output)
    @Valid
    public AnnouncementDimensionsDTO dimensions;

    @Valid
    public AnnouncementWeightDTO weight;

    // Posting place
    @NotBlank(message = "{error.postingPlace.required}")
    public String postingPlace;

    @Range(min = -90, max = 90, message = "{error.posting.latitude.range}")
    public double postingLatitude;

    @Range(min = -180, max = 180, message = "{error.posting.longitude.range}")
    public double postingLongitude;


    // Reception place
    @NotBlank(message = "{error.receptionPlace.required}") //error.place.same
    public String receptionPlace;

    @Range(min = -90, max = 90, message = "{error.reception.latitude.range}")
    public double receptionLatitude;

    @Range(min = -180, max = 180, message = "{error.reception.longitude.range}") //error.coordinates.same
    public double receptionLongitude;


    @NotBlank(message = "{error.postingDateTime.required}")
    public LocalDateTime postingDateTime;

    @NotBlank(message = "{error.receptionDateTime.required}") //error.date.order
    public LocalDateTime receptionDateTime;

    @Valid
    @NotNull(message = "{error.translation.required}")
    @Size(min = 1, max = 1, message = "{error.translation.single}")
    public List<AnnouncementTranslationDTO> translations;

    @Valid
    public List<StopDTO> stops;

    @Valid
    public List<PhotoDTO> photos;

    public AnnouncementDTO() {
    }
}
