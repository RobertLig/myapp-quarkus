package org.example.myapp.dto;

import java.time.LocalDateTime;

public class AnnouncementSearchDTO {

    // Type filter
    public String type; // "sender" / "courier"

    // Place name filters
    public String postingPlace;
    public String receptionPlace;

    // Coordinate filters (NEW)
    public Double postingLatitude;
    public Double postingLongitude;

    public Double receptionLatitude;
    public Double receptionLongitude;

    // Optional radius search (NEW)
    public Double radiusKm; // search announcements within X km from posting coords

    // Date filters
    public LocalDateTime postingFrom;
    public LocalDateTime postingTo;

    public LocalDateTime receptionFrom;
    public LocalDateTime receptionTo;

    // Dimensions filters
    public Double minWidth;
    public Double maxWidth;

    public Double minHeight;
    public Double maxHeight;

    public Double minLength;
    public Double maxLength;

    // Weight filters
    public Double minWeight;
    public Double maxWeight;

    // User filter
    public Long userId; // optional

    // Text search
    public String text;      // search phrase
    public String language;  // optional: "en", "pl"

    // Sorting
    public String sortBy;   // "postingDate", "weight", "width", etc.
    public String sortDir;  // "asc" or "desc"
}
