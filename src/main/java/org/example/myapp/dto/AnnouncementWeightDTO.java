package org.example.myapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public class AnnouncementWeightDTO {

    public double value;

    public String unit; // "metric" or "imperial"
}
