package org.example.myapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class SendMessageRequest {

    @NotNull(message = "{error.conversation.required}")
    @Positive(message = "{error.conversation.invalid}")
    public Long conversationId;

    @NotBlank(message = "{error.content.blank}")
    @Size(max = 2000, message = "{error.content.toolong}")
    public String content;
}