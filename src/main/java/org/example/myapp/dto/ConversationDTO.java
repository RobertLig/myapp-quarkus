package org.example.myapp.dto;

import java.util.Date;
import java.util.List;

public class ConversationDTO {
    public Long id;
    public List<UserResponseDTO> participants;
    public Date createdAt;
}
