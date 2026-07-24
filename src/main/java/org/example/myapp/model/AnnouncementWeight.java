package org.example.myapp.model;

import jakarta.persistence.Embeddable;

@Embeddable
public class AnnouncementWeight {

    private double value;

    // Required by JPA
    protected AnnouncementWeight() {
    }

    // Custom constructor
    public AnnouncementWeight(double value) {
        this.value = value;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }
}
