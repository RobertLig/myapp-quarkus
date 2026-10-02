package org.example.myapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

public class AnnouncementWeightDTO {

    @Positive(message = "{error.weight.positive}")
    public double value;

    @NotBlank(message = "{error.weight.unit.required}")
    @Pattern(regexp = "^(metric|imperial)$", message = "{error.weight.unit.invalid}")
    public String unit; // "metric" or "imperial"
}
