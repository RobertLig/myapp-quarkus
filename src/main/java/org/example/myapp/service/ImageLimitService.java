package org.example.myapp.service;

import jakarta.enterprise.context.ApplicationScoped;
import org.example.myapp.model.Announcement;
import org.example.myapp.model.Message;
import org.example.myapp.model.User;

@ApplicationScoped
public class ImageLimitService {

    // You can easily extend this map later for other entities
    private static final int MAX_ANNOUNCEMENT_PHOTOS = 3;
    private static final int MAX_USER_AVATAR = 1;
    private static final int MAX_MESSAGE_ATTACHMENTS = 10;

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
    // MESSAGE ATTACHMENT LIMIT
    // ------------------------------------------------------------

    public boolean canAddMessageAttachment(Message message) {
        if (message == null) return false;
        return message.getAttachments().size() < MAX_MESSAGE_ATTACHMENTS;
    }

    public int remainingMessageAttachments(Message message) {
        if (message == null) return 0;
        return MAX_MESSAGE_ATTACHMENTS - message.getAttachments().size();
    }

    // ------------------------------------------------------------
    // GENERIC DYNAMIC CHECK (optional)
    // ------------------------------------------------------------

    public boolean canAdd(String entityType, int currentCount) {
        return switch (entityType.toLowerCase()) {
            case "announcement" -> currentCount < MAX_ANNOUNCEMENT_PHOTOS;
            case "user" -> currentCount < MAX_USER_AVATAR;
            case "message" -> currentCount < MAX_MESSAGE_ATTACHMENTS;
            default -> false;
        };
    }

    public int remaining(String entityType, int currentCount) {
        return switch (entityType.toLowerCase()) {
            case "announcement" -> MAX_ANNOUNCEMENT_PHOTOS - currentCount;
            case "user" -> MAX_USER_AVATAR - currentCount;
            case "message" -> MAX_MESSAGE_ATTACHMENTS - currentCount;
            default -> 0;
        };
    }
}
