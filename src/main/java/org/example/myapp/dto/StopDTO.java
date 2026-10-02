package org.example.myapp.dto;

import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.Range;

public class StopDTO {

    public Long id;

    @NotBlank(message = "{error.stop.address.required}")
    public String address;

    @NotNull(message = "{error.stop.latitude.required}")
    @Range(min = -90, max = 90, message = "{error.stop.latitude.range}")
    public Double latitude;

    @NotNull(message = "{error.stop.longitude.required}")
    @Range(min = -180, max = 180, message = "{error.stop.longitude.range}")
    public Double longitude;


    public Integer position;
}



