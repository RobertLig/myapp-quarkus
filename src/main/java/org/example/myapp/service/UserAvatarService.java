package org.example.myapp.service;

import org.jboss.resteasy.reactive.multipart.FileUpload;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
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
            throw new IllegalArgumentException("photo.empty");
        }

        // 2. Validate User & Business Rules
        User user = userService.getUserById(userId)
                .orElseThrow(() -> new WebApplicationException("error.user.notfound", 404));

        if (!imageLimitService.canAddUserAvatar(user)) {
            throw new WebApplicationException("avatar.limit", 400);
        }

        // 3. Read Temp File Bytes
        byte[] fileBytes;
        try {
            fileBytes = Files.readAllBytes(fileUpload.uploadedFile());
        } catch (IOException e) {
            throw new RuntimeException("Failed to read uploaded file", e);
        }

        // 4. Delete Old Avatar from S3 if present
        /* if (user.getPhotoUrl() != null && !user.getPhotoUrl().isBlank()) {
            try {
                imageStoreService.delete(user.getPhotoUrl());
            } catch (Exception ignored) {
                // Prevent cleanup failures from blocking the new upload
            }
        } */

        // 5. Upload New Avatar to S3 & Update Entity
        String url = imageStoreService.upload(fileBytes);
        user.setPhotoUrl(url);

        return url;
    }

    public void deleteAvatar(Long userId) {
        User user = userService.getUserById(userId)
                .orElseThrow(() -> new WebApplicationException("error.user.notfound", 404));

        if (user.getPhotoUrl() == null) {
            throw new WebApplicationException("avatar.none", 400);
        }

        imageStoreService.delete(user.getPhotoUrl());
        user.setPhotoUrl(null);
    }
}
