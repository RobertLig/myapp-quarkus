package org.example.myapp.dto;

import java.time.LocalDateTime;

public class AnnouncementSearchDTO {

    public String type; // sender / courier

    public String postingPlace;
    public String receptionPlace;

    public LocalDateTime postingFrom;
    public LocalDateTime postingTo;

    public LocalDateTime receptionFrom;
    public LocalDateTime receptionTo;

    public Double minWidth;
    public Double maxWidth;

    public Double minHeight;
    public Double maxHeight;

    public Double minLength;
    public Double maxLength;

    public Double minWeight;
    public Double maxWeight;

    public Long userId; // optional
}
