package org.example.myapp.dto;

import jakarta.validation.constraints.*;

public class SenderDTO {

    public Long id;

    @NotBlank(message = "Sender name is required")
    public String name;

    @NotBlank(message = "Sender phone is required")
    @Pattern(regexp = "\\+?[0-9\\- ]+", message = "Invalid phone number format")
    public String phone;

    @Email(message = "Invalid email format")
    public String email;
}


