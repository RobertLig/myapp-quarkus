package org.example.myapp.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class ImagePresignRequestDTO {
    @NotBlank
    @Pattern(regexp = "^image/(jpeg|jpg|png)$", message = "contentType.invalid")
    public String contentType;

    @NotNull
    @Min(value = 1)
    @Max(value = 512000)
    public Long fileSize;
}
