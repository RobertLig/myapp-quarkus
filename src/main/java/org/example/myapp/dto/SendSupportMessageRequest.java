package org.example.myapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class SendSupportMessageRequest {

    @NotBlank(message = "error.content.blank")
    @Size(max = 2000, message = "error.content.toolong")
    public String content;
}