package org.example.myapp.mapper;

import org.example.myapp.dto.UserResponseDTO;
import org.example.myapp.dto.UserProfileResponseDTO;
import org.example.myapp.model.User;

import java.util.function.Function;

public class UserMapper {

    public static UserResponseDTO toDTO(User user) {
        return toDTO(user, Function.identity());
    }

    public static UserResponseDTO toDTO(User user, Function<String, String> readUrl) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setPhotoUrl(readUrl.apply(user.getPhotoUrl()));
        return dto;
    }

    public static UserProfileResponseDTO toProfileDTO(User user) {
        return toProfileDTO(user, Function.identity());
    }

    public static UserProfileResponseDTO toProfileDTO(User user, Function<String, String> readUrl) {
        UserProfileResponseDTO dto = new UserProfileResponseDTO();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setPhotoUrl(readUrl.apply(user.getPhotoUrl()));
        dto.setAgeRange(user.getAgeRange());
        dto.setGender(user.getGender());
        dto.setPhone(user.getPhone());
        dto.setRole(user.getRole());
        return dto;
    }
}
