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
import java.util.List;

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
        if (fileUpload == null || fileUpload.uploadedFile() == null) {
            throw new FieldValidationException("photo", "photo.empty"); //upload up to three photos (photo)
        }

        Announcement announcement = announcementRepository.findByIdOptional(announcementId)
                .orElseThrow(() -> new EntityNotFoundException("error.announcement.notfound"));

        // Authorization Check
        if (!announcement.getUser().getId().equals(currentUserId)) {
            throw DomainException.forbidden("error.unauthorized.access");
        }

        // Limit Check
        if (!imageLimitService.canAddAnnouncementPhoto(announcement)) {
            throw new FieldValidationException("photo", "photo.limit"); //up to three photos (photo)
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

    public PhotoDTO toDTO(Photo photo) {
        PhotoDTO dto = new PhotoDTO();
        dto.id = photo.getId();
        dto.url = photo.getUrl();
        dto.position = photo.getPosition();
        return dto;
    }
}