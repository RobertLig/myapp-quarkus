package org.example.myapp.mapper;

import org.example.myapp.dto.UserResponseDTO;
import org.example.myapp.model.User;

public class UserMapper {

    public static UserResponseDTO toDTO(User user) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setPhotoUrl(user.getPhotoUrl());
        return dto;
    }
}
