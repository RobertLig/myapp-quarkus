package org.example.myapp.dto;

import jakarta.validation.constraints.*;

public class AnnouncementDimensionsDTO {

    @Positive(message = "Width must be positive")
    public double width;

    @Positive(message = "Height must be positive")
    public double height;

    @Positive(message = "Length must be positive")
    public double length;

    @NotBlank(message = "Unit is required")
    public String unit;
}


