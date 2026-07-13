package org.example.myapp.model;

import jakarta.persistence.Embeddable;

@Embeddable
public class AnnouncementWeight {
    private double value;
    private String unit; // "kg" or "lb"

    // getters and setters
}
