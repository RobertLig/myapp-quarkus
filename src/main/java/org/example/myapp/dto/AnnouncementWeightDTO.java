package org.example.myapp.dto;

import jakarta.validation.constraints.*;

public class AnnouncementWeightDTO {

    @Positive(message = "Weight value must be positive")
    public double value;

    @NotBlank(message = "Unit is required")
    public String unit;
}



