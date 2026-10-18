package org.example.myapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class AdminReplyRequest {

    @NotNull(message = "error.conversationid.null")
    public Long conversationId;

    @NotBlank(message = "error.content.blank")
    @Size(max = 2000, message = "error.content.toolong")
    public String content;
}