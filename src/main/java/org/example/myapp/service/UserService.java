package org.example.myapp.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.example.myapp.dto.UserDTO;
import org.example.myapp.model.User;
import org.example.myapp.repository.UserRepository;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.util.Optional;

@ApplicationScoped
public class UserService {

    @Inject
    UserRepository userRepository;

    public User register(UserDTO dto) {

        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Email already in use");
        }

        User user = new User();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPassword(hashPassword(dto.getPassword()));

        userRepository.persist(user);
        return user;
    }

    public User login(String email, String password) {
        User user = userRepository.findByEmail(email);

        if (user == null) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        if (!verifyPassword(password, user.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        return user;
    }

    public User updateProfile(Long userId, UserDTO dto) {
        User user = userRepository.findById(userId);

        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }

        // Required fields (update only if provided)
        if (dto.getName() != null && !dto.getName().isBlank()) {
            user.setName(dto.getName());
        }

        if (dto.getEmail() != null && !dto.getEmail().isBlank()) {
            user.setEmail(dto.getEmail());
        }

        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            user.setPassword(hashPassword(dto.getPassword()));
        }

        // Optional fields (update only if provided)
        if (dto.getAgeRange() != null) {
            user.setAgeRange(dto.getAgeRange());
        }

        if (dto.getGender() != null) {
            user.setGender(dto.getGender());
        }

        if (dto.getPhone() != null) {
            user.setPhone(dto.getPhone());
        }

        if (dto.getPhotoUrl() != null) {
            user.setPhotoUrl(dto.getPhotoUrl());
        }

        return user;
    }

    public boolean deleteUser(Long userId) {
        User user = userRepository.findById(userId);

        if (user == null) {
            return false; // user does not exist
        }

        userRepository.delete(user);
        return true;
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findByIdOptional(id);
    }

    // PBKDF2 hashing
    private String hashPassword(String password) {
        try {
            char[] chars = password.toCharArray();
            byte[] salt = "staticSalt123".getBytes();

            PBEKeySpec spec = new PBEKeySpec(chars, salt, 65536, 256);
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] hash = skf.generateSecret(spec).getEncoded();

            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private boolean verifyPassword(String password, String storedHash) {
        return storedHash.equals(hashPassword(password));
    }
}
