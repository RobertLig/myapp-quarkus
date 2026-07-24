package org.example.myapp.model;

import jakarta.persistence.Embeddable;

@Embeddable
public class AnnouncementDimensions {

    private double width;
    private double height;
    private double length;

    // Required by JPA
    protected AnnouncementDimensions() {
    }

    // Custom constructor for convenience
    public AnnouncementDimensions(double width, double height, double length) {
        this.width = width;
        this.height = height;
        this.length = length;
    }

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
}
