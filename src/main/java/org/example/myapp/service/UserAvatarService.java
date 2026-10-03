package org.example.myapp.service;

import org.example.myapp.exception.EntityNotFoundException;
import org.example.myapp.exception.FieldValidationException;
import org.jboss.resteasy.reactive.multipart.FileUpload;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.example.myapp.model.User;

import java.io.IOException;
import java.nio.file.Files;

@ApplicationScoped
public class UserAvatarService {

    @Inject
    UserService userService;

    @Inject
    ImageLimitService imageLimitService;

    @Inject
    ImageStoreService imageStoreService;

    @Transactional
    public String uploadAvatar(Long userId, FileUpload fileUpload) {
        // 1. Validate File Upload Presence
        if (fileUpload == null || fileUpload.uploadedFile() == null) {
            throw new FieldValidationException("avatar", "photo.empty");
        }

        // 2. Validate User & Business Rules
        User user = userService.getUserById(userId)
                .orElseThrow(() -> new EntityNotFoundException("error.user.notfound"));

        if (!imageLimitService.canAddUserAvatar(user)) {
            throw new FieldValidationException("avatar", "avatar.limit");
        }

        // 3. Read Temp File Bytes
        byte[] fileBytes;
        try {
            fileBytes = Files.readAllBytes(fileUpload.uploadedFile());
        } catch (IOException e) {
            throw new RuntimeException("error.internal", e);
        }

        // 5. Upload New Avatar to S3 & Update Entity
        String url = imageStoreService.upload(fileBytes);
        user.setPhotoUrl(url);

        return url;
    }

    @Transactional
    public void deleteAvatar(Long userId) {
        User user = userService.getUserById(userId)
                .orElseThrow(() -> new EntityNotFoundException("error.user.notfound"));

        if (user.getPhotoUrl() == null) {
            throw new FieldValidationException("avatar", "avatar.none");
        }

        imageStoreService.delete(user.getPhotoUrl());
        user.setPhotoUrl(null);
    }
}
