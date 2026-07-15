package org.example.myapp.dto;

import jakarta.validation.constraints.*;

public class CourierDTO {

    public Long id;

    @NotBlank(message = "Courier name is required")
    public String name;

    @NotBlank(message = "Courier phone is required")
    @Pattern(regexp = "\\+?[0-9\\- ]+", message = "Invalid phone number format")
    public String phone;
}


