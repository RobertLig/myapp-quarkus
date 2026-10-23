package org.example.myapp.dto;

import jakarta.validation.constraints.NotNull;

public class ReactionRequest {
    @NotNull(message = "messageId.required")
    public Long messageId;

    public String reaction; // ❤️ 👍 😂 or null to remove
}
