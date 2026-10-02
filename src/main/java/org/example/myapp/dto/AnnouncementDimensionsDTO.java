package org.example.myapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

public class AnnouncementDimensionsDTO {

    @Positive(message = "{error.dimensions.width.positive}")
    public double width;

    @Positive(message = "{error.dimensions.height.positive}")
    public double height;

    @Positive(message = "{error.dimensions.length.positive}")
    public double length;

    // "metric" or "imperial"
    @NotBlank(message = "{error.dimensions.unit.required}")
    @Pattern(regexp = "^(metric|imperial)$", message = "{error.dimensions.unit.invalid}")
    public String unit;

    public AnnouncementDimensionsDTO() {
    }

    public AnnouncementDimensionsDTO(double width, double height, double length, String unit) {
        this.width = width;
        this.height = height;
        this.length = length;
        this.unit = unit;
    }
}
