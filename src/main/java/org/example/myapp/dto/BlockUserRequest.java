package org.example.myapp.dto;

import jakarta.validation.constraints.NotNull;

public class BlockUserRequest {

    @NotNull(message = "{error.user.required}")
    public Long blockedId;
}
