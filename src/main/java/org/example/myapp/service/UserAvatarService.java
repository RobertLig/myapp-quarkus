package org.example.myapp.service;

import org.example.myapp.exception.EntityNotFoundException;
import org.example.myapp.exception.FieldValidationException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.example.myapp.dto.ImageUploadCompleteRequestDTO;
import org.example.myapp.dto.PresignResponseDTO;
import org.example.myapp.model.User;

@ApplicationScoped
public class UserAvatarService {

    @Inject
    UserService userService;

    @Inject
    ImageLimitService imageLimitService;

    @Inject
    ImageStoreService imageStoreService;

    @Inject
    ImageUploadPresignService imageUploadPresignService;

    @Transactional
    public PresignResponseDTO presignAvatar(Long userId, String contentType, Long fileSize) {
        User user = userService.getUserById(userId)
                .orElseThrow(() -> new EntityNotFoundException("error.user.notfound"));

        if (!imageLimitService.canAddUserAvatar(user)) {
            throw new FieldValidationException("avatar", "avatar.limit");
        }

        return imageUploadPresignService.presign(contentType, fileSize);
    }

    @Transactional
    public String completeAvatarUpload(Long userId, ImageUploadCompleteRequestDTO request) {
        User user = userService.getUserById(userId)
                .orElseThrow(() -> new EntityNotFoundException("error.user.notfound"));

        if (!imageLimitService.canAddUserAvatar(user)) {
            throw new FieldValidationException("avatar", "avatar.limit");
        }

        imageUploadPresignService.verifyUploadedImage(request.fileUrl, request.contentType, request.fileSize);
        user.setPhotoUrl(request.fileUrl);
        return request.fileUrl;
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
