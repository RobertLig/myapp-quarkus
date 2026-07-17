package org.example.myapp.model;

import jakarta.persistence.Embeddable;

@Embeddable
public class AnnouncementWeight {

    private double value;


    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }
}
