package org.example.myapp.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import org.example.myapp.model.User;

@ApplicationScoped
public class UserAvatarService {

    @Inject
    ImageStoreService imageStoreService;
    @Inject ImageLimitService imageLimitService;
    @Inject UserService userService;

    public String uploadAvatar(Long userId, byte[] file) {
        User user = userService.getUserById(userId)
                .orElseThrow(() -> new WebApplicationException("error.user.notfound", 404));

        if (!imageLimitService.canAddUserAvatar(user)) {
            throw new WebApplicationException("avatar.limit", 400);
        }

        String url = imageStoreService.upload(file);
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
