package org.example.myapp.model;

import jakarta.persistence.Embeddable;

@Embeddable
public class AnnouncementDimensions {
    private double width;
    private double height;
    private double length;

    private String unit; // "cm" or "in"

    // getters and setters
    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public double getLength() {
        return length;
    }

    public void setLength(double length) {
        this.length = length;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }
}
