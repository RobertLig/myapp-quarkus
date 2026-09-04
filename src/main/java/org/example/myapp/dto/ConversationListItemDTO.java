package org.example.myapp.dto;

import java.util.Date;
import java.util.List;

public class ConversationListItemDTO {
    public Long id;
    public Date createdAt;
    public Date deletedAt; // per-user delete
    public List<UserResponseDTO> participants;
    public Integer unreadCount;
    public boolean otherUserOnline;
}
