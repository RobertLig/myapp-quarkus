package org.example.myapp.model;

import jakarta.persistence.Embeddable;

@Embeddable
public class AnnouncementWeight {
    private double weight;
    private String unit; // "kg" or "lb"

    // getters and setters
    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }
}
