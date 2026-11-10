package org.example.myapp.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.example.myapp.auth.GooglePayload;
import org.example.myapp.auth.GoogleService;
import org.example.myapp.dto.UserDTO;
import org.example.myapp.exception.EntityNotFoundException;
import org.example.myapp.exception.FieldValidationException;
import org.example.myapp.model.Locale;
import org.example.myapp.model.User;
import org.example.myapp.model.Announcement;
import org.example.myapp.model.Photo;
import org.example.myapp.repository.UserRepository;
import org.example.myapp.repository.AnnouncementRepository;
import org.example.myapp.repository.ConversationParticipantRepository;
import org.example.myapp.repository.ConversationRepository;
import org.example.myapp.repository.MessageAttachmentRepository;
import org.example.myapp.repository.MessageEventRepository;
import org.example.myapp.repository.MessageReactionRepository;
import org.example.myapp.repository.MessageRepository;

import java.util.Optional;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class UserService {

    @Inject
    UserRepository userRepository;

    @Inject
    AnnouncementRepository announcementRepository;

    @Inject
    EntityManager entityManager;

    @Inject
    ImageStoreService imageStoreService;

    @Inject
    GoogleService googleService;

    @Inject
    org.example.myapp.repository.UserBlockRepository userBlockRepository;

    @Inject
    ConversationRepository conversationRepository;

    @Inject
    ConversationParticipantRepository conversationParticipantRepository;

    @Inject
    MessageRepository messageRepository;

    @Inject
    MessageEventRepository messageEventRepository;

    @Inject
    MessageReactionRepository messageReactionRepository;

    @Inject
    MessageAttachmentRepository messageAttachmentRepository;

    @Transactional
    public User updateProfile(Long userId, UserDTO dto) {
        User user = userRepository.findById(userId);

        if (user == null) {
            throw new EntityNotFoundException("error.user.notfound");
        }

        // Name
        if (dto.getName() != null && !dto.getName().isBlank()) {
            user.setName(dto.getName());
        }

        // Email (with uniqueness check)
        if (dto.getEmail() != null && !dto.getEmail().isBlank()) {

            if (!dto.getEmail().equals(user.getEmail()) &&
                    userRepository.existsByEmail(dto.getEmail())) {

                throw new FieldValidationException("email", "error.email.inuse");
            }

            user.setEmail(dto.getEmail());
        }

        Locale locale = dto.getLocale() != null
                ? dto.getLocale()
                : Locale.en;

        user.setLocale(locale);

        if (dto.isAgeRangeProvided()) user.setAgeRange(dto.getAgeRange());
        if (dto.isGenderProvided()) user.setGender(dto.getGender());
        if (dto.isPhoneProvided()) user.setPhone(dto.getPhone());
        if (dto.getPhotoUrl() != null) user.setPhotoUrl(dto.getPhotoUrl());

        return user;
    }

    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findByIdOptional(userId)
                .orElseThrow(() -> new EntityNotFoundException("error.user.notfound"));

        // Clean up UserBlock records where this user is either blocker or blocked
        userBlockRepository.deleteByBlockerOrBlocked(userId);

        // Fetch user's announcements to gather S3 photo URLs before cascading DB deletion
        List<Announcement> announcements = announcementRepository.list("user.id", userId);

        List<String> photoUrls = announcements.stream()
                .flatMap(a -> a.getPhotos().stream())
                .map(Photo::getUrl)
                .collect(Collectors.toList());
        List<String> attachmentUrls = messageAttachmentRepository.findByUserId(userId).stream()
                .map(org.example.myapp.model.MessageAttachment::getUrl)
                .toList();
        photoUrls.addAll(attachmentUrls);
        if (user.getPhotoUrl() != null) {
            photoUrls.add(user.getPhotoUrl());
        }

        // Keep shared conversations and messages, but remove their references to this account.
        announcements.forEach(announcement -> conversationRepository.nullifyAnnouncement(announcement.getId()));
        // Bulk updates bypass managed entities; clear stale conversation-to-announcement references.
        entityManager.flush();
        entityManager.clear();

        user = userRepository.findByIdOptional(userId)
                .orElseThrow(() -> new EntityNotFoundException("error.user.notfound"));
        announcements = announcementRepository.list("user.id", userId);
        for (Announcement announcement : announcements) {
            announcementRepository.delete(announcement);
        }
        messageAttachmentRepository.deleteByUserId(userId);
        messageReactionRepository.deleteByUserId(userId);
        messageEventRepository.deleteByUserId(userId);
        conversationParticipantRepository.deleteByUserId(userId);
        messageRepository.nullifySender(userId);

        // Delete the user after removing all foreign-key references to it.
        userRepository.delete(user);

        // Clean up files stored in AWS S3
        imageStoreService.deleteAll(photoUrls);
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findByIdOptional(id);
    }

    @Transactional
    public User loginWithGoogle(String idToken, Locale preferredLocale) {

        GooglePayload payload = googleService.verify(idToken);

        String email = payload.getEmail();
        String name = payload.getName();
        String googleId = payload.getSubject();

        // 1️⃣ First: try to find by googleId (best, stable identifier)
        User user = userRepository.find("googleId", googleId).firstResult();

        // 2️⃣ Second: fallback — find by email (handles first-time Google login)
        if (user == null) {
            user = userRepository.find("email", email).firstResult();
        }

        // 3️⃣ If still null → create new user
        if (user == null) {
            user = new User();
            user.setEmail(email);
            user.setName(name);
            user.setGoogleId(googleId);
            userRepository.persist(user);
        } else {
            // 4️⃣ If user exists but googleId is missing → attach it
            if (user.getGoogleId() == null) {
                user.setGoogleId(googleId);
            }
        }

        if (user.getLocale() == null) {
            user.setLocale(preferredLocale != null ? preferredLocale : Locale.en);
        }

        return user;
    }
}
