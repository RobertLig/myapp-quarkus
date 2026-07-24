package org.example.myapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public class AnnouncementWeightDTO {

    @Positive(message = "Weight value must be positive")
    public double value;

    @NotBlank(message = "Unit is required")
    public String unit; // "metric" or "imperial"
}
