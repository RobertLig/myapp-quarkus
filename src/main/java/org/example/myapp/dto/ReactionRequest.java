package org.example.myapp.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ReactionRequest {
    @NotNull(message = "messageId.required")
    public Long messageId;

    @Size(max = 10, message = "reaction.tooLong")
    public String reaction; // ❤️ 👍 😂 or null to remove
}
