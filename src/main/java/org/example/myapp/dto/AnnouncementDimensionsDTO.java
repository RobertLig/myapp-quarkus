package org.example.myapp.dto;

public class AnnouncementDimensionsDTO {

    public double width;
    public double height;
    public double length;

    // "metric" or "imperial"
    public String unit;

    public AnnouncementDimensionsDTO() {
    }

    public AnnouncementDimensionsDTO(double width, double height, double length, String unit) {
        this.width = width;
        this.height = height;
        this.length = length;
        this.unit = unit;
    }
}
