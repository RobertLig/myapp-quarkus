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
import jakarta.ws.rs.WebApplicationException;

@ApplicationScoped
public class UserService {

    @Inject
    UserRepository userRepository;

    @Inject
    EmailService emailService;

    public User register(UserDTO dto) {

        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new WebApplicationException("error.email.inuse", 400);
        }

        User user = new User();

        // Basic fields
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());

        // Generate salt
        String salt = generateSalt();
        user.setSalt(salt);

        // Hash password with salt
        user.setPassword(hashPassword(dto.getPassword(), salt));

        // Email verification
        String token = generateVerificationToken();
        user.setVerificationToken(token);
        user.setEmailVerified(false);

        userRepository.persist(user);

        // Send email (or return link in dev mode)
        emailService.sendVerificationEmail(user.getEmail(), token);

        return user;
    }

    public User login(String email, String password) {
        User user = userRepository.findByEmail(email);

        if (user == null) {
            throw new WebApplicationException("error.login.invalid", 401);
        }

        if (!verifyPassword(password, user.getPassword(), user.getSalt())) {
            throw new WebApplicationException("error.login.invalid", 401);
        }

        if (!user.isEmailVerified()) {
            throw new WebApplicationException("error.email.notverified", 403);
        }

        return user;
    }

    public User updateProfile(Long userId, UserDTO dto) {
        User user = userRepository.findById(userId);

        if (user == null) {
            throw new WebApplicationException("error.user.notfound", 404);
        }

        // Name
        if (dto.getName() != null && !dto.getName().isBlank()) {
            user.setName(dto.getName());
        }

        // Email (with uniqueness check)
        if (dto.getEmail() != null && !dto.getEmail().isBlank()) {

            if (!dto.getEmail().equals(user.getEmail()) &&
                    userRepository.existsByEmail(dto.getEmail())) {

                throw new WebApplicationException("error.email.inuse", 400);
            }

            user.setEmail(dto.getEmail());
        }

        // Password (hash with existing salt)
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            user.setPassword(hashPassword(dto.getPassword(), user.getSalt()));
        }

        // Optional fields
        if (dto.getAgeRange() != null) user.setAgeRange(dto.getAgeRange());
        if (dto.getGender() != null) user.setGender(dto.getGender());
        if (dto.getPhone() != null) user.setPhone(dto.getPhone());
        if (dto.getPhotoUrl() != null) user.setPhotoUrl(dto.getPhotoUrl());

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
    private String hashPassword(String password, String salt) {
        try {
            PBEKeySpec spec = new PBEKeySpec(
                    password.toCharArray(),
                    Base64.getDecoder().decode(salt),
                    65536,
                    256
            );

            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] hash = skf.generateSecret(spec).getEncoded();

            return Base64.getEncoder().encodeToString(hash);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String generateSalt() {
        byte[] salt = new byte[16];
        new java.security.SecureRandom().nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    private boolean verifyPassword(String password, String storedHash, String salt) {
        String hash = hashPassword(password, salt);
        return storedHash.equals(hash);
    }

    private String generateVerificationToken() {
        byte[] bytes = new byte[32];
        new java.security.SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
