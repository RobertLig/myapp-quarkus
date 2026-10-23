package org.example.myapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

public class AttachmentRequest {
    @NotNull(message = "messageId.required")
    public Long messageId;

    @NotBlank(message = "url.required")
    @Pattern(regexp = "^https?://.*", message = "url.invalid")
    public String url;

    @NotBlank(message = "type.required")
    @Pattern(regexp = "^[a-zA-Z]+/[a-zA-Z0-9+\\-._]+$", message = "type.invalid")
    public String type;

    @NotNull(message = "size.required")
    @Positive(message = "size.invalid")
    public Long size;
}
