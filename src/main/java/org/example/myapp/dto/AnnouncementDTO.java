package org.example.myapp.dto;

import java.time.LocalDateTime;
import java.util.List;

public class AnnouncementDTO {
    public Long id;
    public String title;
    public String description;
    public LocalDateTime createdAt;

    public SenderDTO sender;
    public CourierDTO courier;

    public AnnouncementDimensionsDTO dimensions;
    public AnnouncementWeightDTO weight;

    public List<StopDTO> stops;
    public List<PhotoDTO> photos;
}
