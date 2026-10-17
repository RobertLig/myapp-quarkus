package org.example.myapp.dto;

import jakarta.validation.constraints.NotNull;

public class ListMessagesRequest {

    @NotNull(message = "error.conversationId.required")
    public Long conversationId;
}