package org.example.myapp.dto;

import java.time.LocalDateTime;
import java.util.List;

public class AnnouncementDTO {
    public Long id;
    public String title;
    public String description;

    public AnnouncementDimensionsDTO dimensions;
    public AnnouncementWeightDTO weight;

    public String postingPlace;
    public String receptionPlace;

    public LocalDateTime postingDateTime;
    public LocalDateTime receptionDateTime;

    public SenderDTO sender;
    public CourierDTO courier;

    public List<StopDTO> stops;
    public List<PhotoDTO> photos;
}

