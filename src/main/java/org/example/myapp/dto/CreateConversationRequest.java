package org.example.myapp.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public class CreateConversationRequest {
    @NotEmpty(message = "error.participantIDs.required")
    public List<Long> participantIds;

    public Long announcementId; // Optional: Announcement
}