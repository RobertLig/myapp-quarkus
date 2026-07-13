package org.example.myapp.model;

import jakarta.persistence.Embeddable;

@Embeddable
public class AnnouncementDimensions {
    private double width;
    private double height;
    private double length;

    private String unit; // "cm" or "in"

    // getters and setters
}
