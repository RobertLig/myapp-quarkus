package org.example.myapp.mapper;

import org.example.myapp.dto.UserResponseDTO;
import org.example.myapp.model.User;

public class UserMapper {

    public static UserResponseDTO toDTO(User user) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.id = user.getId();
        dto.name = user.getName();
        dto.email = user.getEmail();
        dto.photoUrl = user.getPhotoUrl();
        return dto;
    }
}
