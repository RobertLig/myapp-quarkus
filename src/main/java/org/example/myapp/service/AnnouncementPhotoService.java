package org.example.myapp.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.example.myapp.exception.EntityNotFoundException; //check it
import jakarta.transaction.Transactional;
import org.example.myapp.dto.PhotoDTO;
import org.example.myapp.exception.DomainException;
import org.example.myapp.exception.FieldValidationException;
import org.example.myapp.model.Announcement;
import org.example.myapp.model.Photo;
import org.example.myapp.repository.AnnouncementRepository;
import org.example.myapp.repository.PhotoRepository;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@ApplicationScoped
public class AnnouncementPhotoService {

    @Inject
    AnnouncementRepository announcementRepository;

    @Inject
    PhotoRepository photoRepository;

    @Inject
    ImageStoreService imageStoreService;

    @Inject
    ImageLimitService imageLimitService;

    @Transactional
    public PhotoDTO uploadPhoto(Long announcementId, Long currentUserId, FileUpload fileUpload) {

        // Limit: Max 3 photo uploads per user per hour
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        long recentPhotosCount = photoRepository.count(
                "announcement.user.id = ?1 and createdAt >= ?2", currentUserId, oneHourAgo
        );

        if (recentPhotosCount >= 3) {
            throw DomainException.tooManyRequests("error.rate.limit");
        }

        if (fileUpload == null || fileUpload.uploadedFile() == null) {
            throw new FieldValidationException("photo", "photo.empty"); //photos? (photo?)
        }

        Announcement announcement = announcementRepository.findByIdOptional(announcementId)
                .orElseThrow(() -> new EntityNotFoundException("error.announcement.notfound"));

        // Authorization Check
        if (!announcement.getUser().getId().equals(currentUserId)) {
            throw DomainException.forbidden("error.unauthorized.access");
        }

        // Limit Check
        if (!imageLimitService.canAddAnnouncementPhoto(announcement)) {
            throw new FieldValidationException("photo", "photo.limit"); //photos? (photo?)
        }

        // Read Bytes
        byte[] fileBytes;
        try {
            fileBytes = Files.readAllBytes(fileUpload.uploadedFile());
        } catch (IOException e) {
            throw new RuntimeException("error.internal", e);
        }

        // Upload to S3
        String url = imageStoreService.upload(fileBytes);

        // Next position calculation
        int nextPosition = announcement.getPhotos().size();

        Photo photo = new Photo(url, nextPosition);
        photo.setAnnouncement(announcement);
        announcement.getPhotos().add(photo);

        photoRepository.persist(photo);

        return toDTO(photo);
    }

    @Transactional
    public void deletePhoto(Long announcementId, Long photoId, Long currentUserId) {
        Announcement announcement = announcementRepository.findByIdOptional(announcementId)
                .orElseThrow(() -> new EntityNotFoundException("error.announcement.notfound"));

        if (!announcement.getUser().getId().equals(currentUserId)) {
            throw DomainException.forbidden("error.unauthorized.access");
        }

        Photo photo = photoRepository.findByIdOptional(photoId)
                .orElseThrow(() -> new EntityNotFoundException("error.photo.notfound"));

        if (!photo.getAnnouncement().getId().equals(announcementId)) {
            throw new FieldValidationException("photo", "photo.mismatch");
        }

        // Remove from S3 & Entity list
        imageStoreService.delete(photo.getUrl());
        announcement.getPhotos().remove(photo);
        photoRepository.delete(photo);

        // Re-index remaining positions
        List<Photo> remaining = announcement.getPhotos();
        for (int i = 0; i < remaining.size(); i++) {
            remaining.get(i).setPosition(i);
        }
    }

    @Transactional
    public List<PhotoDTO> sortPhotos(Long announcementId, Long currentUserId, List<PhotoDTO> sortedPhotos) {
        if (sortedPhotos == null || sortedPhotos.isEmpty()) {
            throw new FieldValidationException("photos", "photo.empty"); //photos? (photo?)
        }

        Announcement announcement = announcementRepository.findByIdOptional(announcementId)
                .orElseThrow(() -> new EntityNotFoundException("error.announcement.notfound"));

        // Ownership check
        if (!announcement.getUser().getId().equals(currentUserId)) {
            throw DomainException.forbidden("error.unauthorized.access");
        }

        List<Photo> existingPhotos = announcement.getPhotos();

        // 1. Validate list lengths match
        if (sortedPhotos.size() != existingPhotos.size()) {
            throw new FieldValidationException("photos", "photo.mismatch"); //photos? (photo?)
        }

        // 2. Validate that incoming DTO photo IDs exactly match existing entity IDs
        Set<Long> existingIds = existingPhotos.stream()
                .map(Photo::getId)
                .collect(Collectors.toSet());

        Set<Long> incomingIds = sortedPhotos.stream()
                .map(dto -> dto.id)
                .collect(Collectors.toSet());

        if (!existingIds.equals(incomingIds)) {
            throw new FieldValidationException("photos", "photo.mismatch");
        }

        // 3. Map existing photos by ID for in-memory lookup (0 DB queries in loop)
        Map<Long, Photo> photoMap = existingPhotos.stream()
                .collect(Collectors.toMap(Photo::getId, Function.identity()));

        // 4. Re-assign new positions sequentially based on array index order
        for (int i = 0; i < sortedPhotos.size(); i++) {
            Long photoId = sortedPhotos.get(i).id;
            Photo photo = photoMap.get(photoId);
            photo.setPosition(i);
        }

        // Sort response collection by new position
        existingPhotos.sort((p1, p2) -> Integer.compare(p1.getPosition(), p2.getPosition()));

        return existingPhotos.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<PhotoDTO> setMainPhoto(Long announcementId, Long photoId, Long currentUserId) {
        // 1. Fetch Announcement
        Announcement announcement = announcementRepository.findByIdOptional(announcementId)
                .orElseThrow(() -> new EntityNotFoundException("error.announcement.notfound"));

        // 2. Authorization Check
        if (!announcement.getUser().getId().equals(currentUserId)) {
            throw DomainException.forbidden("error.unauthorized.access");
        }

        // 3. Find target photo inside the announcement's photos
        Photo mainPhoto = announcement.getPhotos().stream()
                .filter(photo -> photo.getId().equals(photoId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("error.photo.notfound"));

        // 4. Reorder positions (Main photo becomes index 0, others shifted)
        mainPhoto.setPosition(0);

        int pos = 1;
        for (Photo photo : announcement.getPhotos()) {
            if (!photo.getId().equals(photoId)) {
                photo.setPosition(pos++);
            }
        }

        // 5. Return sorted list of DTOs
        return announcement.getPhotos().stream()
                .sorted(Comparator.comparingInt(Photo::getPosition))
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public PhotoDTO toDTO(Photo photo) {
        PhotoDTO dto = new PhotoDTO();
        dto.id = photo.getId();
        dto.url = photo.getUrl();
        dto.position = photo.getPosition();
        return dto;
    }
}