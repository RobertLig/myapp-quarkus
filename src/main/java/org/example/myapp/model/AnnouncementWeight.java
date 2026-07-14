package org.example.myapp.model;

import jakarta.persistence.Embeddable;

@Embeddable
public class AnnouncementWeight {

    private double value;
    private String unit;

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }
}
