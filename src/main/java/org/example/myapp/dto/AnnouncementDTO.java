package org.example.myapp.dto;

import java.time.LocalDateTime;
import java.util.List;

public class AnnouncementDTO {

    public Long id;

    public String type; // "sender" or "courier"
    public Long userId;

    // Dimensions (DTO includes unit for input/output)
    public AnnouncementDimensionsDTO dimensions;

    public AnnouncementWeightDTO weight;

    // Posting place
    public String postingPlace;
    public double postingLatitude;
    public double postingLongitude;

    // Reception place
    public String receptionPlace;
    public double receptionLatitude;
    public double receptionLongitude;

    public LocalDateTime postingDateTime;
    public LocalDateTime receptionDateTime;

    public List<AnnouncementTranslationDTO> translations;
    public List<StopDTO> stops;
    public List<PhotoDTO> photos;

    public AnnouncementDTO() {
    }
}
