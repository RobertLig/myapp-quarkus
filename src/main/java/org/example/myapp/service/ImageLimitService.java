package org.example.myapp.service;

import jakarta.enterprise.context.ApplicationScoped;
import org.example.myapp.model.Announcement;
import org.example.myapp.model.User;

@ApplicationScoped
public class ImageLimitService {

    // You can easily extend this map later for other entities
    private static final int MAX_ANNOUNCEMENT_PHOTOS = 3;
    private static final int MAX_USER_AVATAR = 1;

    // ------------------------------------------------------------
    // ANNOUNCEMENT LIMIT
    // ------------------------------------------------------------

    public boolean canAddAnnouncementPhoto(Announcement announcement) {
        if (announcement == null) return false;
        return announcement.getPhotos().size() < MAX_ANNOUNCEMENT_PHOTOS;
    }

    public int remainingAnnouncementPhotos(Announcement announcement) {
        if (announcement == null) return 0;
        return MAX_ANNOUNCEMENT_PHOTOS - announcement.getPhotos().size();
    }

    // ------------------------------------------------------------
    // USER AVATAR LIMIT
    // ------------------------------------------------------------

    public boolean canAddUserAvatar(User user) {
        if (user == null) return false;
        return user.getPhotoUrl() == null; // user can have only 1 avatar
    }

    public int remainingUserAvatars(User user) {
        if (user == null) return 0;
        return user.getPhotoUrl() == null ? 1 : 0;
    }

    // ------------------------------------------------------------
    // GENERIC DYNAMIC CHECK (optional)
    // ------------------------------------------------------------

    public boolean canAdd(String entityType, int currentCount) {
        return switch (entityType.toLowerCase()) {
            case "announcement" -> currentCount < MAX_ANNOUNCEMENT_PHOTOS;
            case "user" -> currentCount < MAX_USER_AVATAR;
            default -> false;
        };
    }

    public int remaining(String entityType, int currentCount) {
        return switch (entityType.toLowerCase()) {
            case "announcement" -> MAX_ANNOUNCEMENT_PHOTOS - currentCount;
            case "user" -> MAX_USER_AVATAR - currentCount;
            default -> 0;
        };
    }
}
