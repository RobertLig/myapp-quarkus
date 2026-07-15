package org.example.myapp.dto;

import jakarta.validation.constraints.*;

public class StopDTO {

    public Long id;

    @NotBlank(message = "Address is required")
    public String address;

    @NotNull(message = "Latitude is required")
    public Double latitude;

    @NotNull(message = "Longitude is required")
    public Double longitude;
}


