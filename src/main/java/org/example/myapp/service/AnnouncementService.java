package org.example.myapp.service;

import org.example.myapp.exception.DomainException;
import org.example.myapp.exception.EntityNotFoundException;
import org.example.myapp.model.*;
import org.example.myapp.dto.*;
import org.example.myapp.repository.AnnouncementRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@ApplicationScoped
public class AnnouncementService {

    @Inject
    AnnouncementRepository announcementRepository;

    @Inject
    UserService userService;

    @Inject
    AnnouncementDimensionsService dimensionsService;

    @Inject
    AnnouncementWeightService weightService;

    @Inject
    AnnouncementTranslationService announcementTranslationService;

    @Inject
    StopService stopService;

    @Inject
    PhotoService photoService;

    @Inject
    ImageStoreService imageStoreService;

    // -----------------------
    // CRUD
    // -----------------------

    public List<Announcement> findAll() {
        return announcementRepository.listAll();
    }

    public Optional<Announcement> findById(Long id) {
        return announcementRepository.findByIdOptional(id);
    }

    @Transactional
    public AnnouncementDTO create(AnnouncementDTO dto, String defaultLang) {

        var user = userService.getUserById(dto.userId);
        if (user.isEmpty()) {
            throw new EntityNotFoundException("error.user.notfound");
        }

        Announcement announcement = new Announcement(dto.type, user.get());

        applyAnnouncementData(announcement, dto, defaultLang);

        announcementRepository.persist(announcement);
        return toAnnouncementDTO(announcement, defaultLang);
    }

    @Transactional
    public AnnouncementDTO update(Long id, AnnouncementDTO dto, Long userId, String defaultLang) {
        Announcement existing = announcementRepository.findByIdOptional(id)
                .orElseThrow(() -> new EntityNotFoundException("error.announcement.notfound"));

        // Ownership check (throws ForbiddenException if not owner or admin)
        checkOwnership(userId, existing);

        // Apply basic fields, translations, dimensions, weight, stops
        applyAnnouncementData(existing, dto, defaultLang);

        return toAnnouncementDTO(existing, defaultLang);
    }

    private void checkOwnership(Long userId, Announcement announcement) {
        if (announcement.getUser() == null || !announcement.getUser().getId().equals(userId)) {
            throw DomainException.forbidden("error.unauthorized.access");
        }
    }

    private void applyAnnouncementData(Announcement announcement, AnnouncementDTO dto, String defaultLang) {

        // --- BASIC FIELDS ---
        announcement.setType(dto.type);
        announcement.setPostingPlace(dto.postingPlace);
        announcement.setPostingLatitude(dto.postingLatitude);
        announcement.setPostingLongitude(dto.postingLongitude);

        announcement.setReceptionPlace(dto.receptionPlace);
        announcement.setReceptionLatitude(dto.receptionLatitude);
        announcement.setReceptionLongitude(dto.receptionLongitude);

        announcement.setPostingDateTime(dto.postingDateTime);
        announcement.setReceptionDateTime(dto.receptionDateTime);

        // --- USER ---
        if (dto.userId != null) {
            var userOpt = userService.getUserById(dto.userId);
            if (userOpt.isEmpty()) {
                throw new EntityNotFoundException("error.user.notfound");
            }
            announcement.setUser(userOpt.get());
        }

        // --- DIMENSIONS & WEIGHT ---
        announcement.setDimensions(dimensionsService.toEntity(dto.dimensions));
        announcement.setWeight(weightService.toEntity(dto.weight));

        // --- TRANSLATIONS ---
        var newTranslations = announcementTranslationService.generateTranslations(
                (dto.translations != null && !dto.translations.isEmpty()) ? dto.translations.get(0) : null,
                announcement,
                defaultLang
        );

        // Mutate existing collection safely (Works for both CREATE and UPDATE)
        if (announcement.getTranslations() == null) {
            announcement.setTranslations(new ArrayList<>(newTranslations));
        } else {
            announcement.getTranslations().clear();
            if (newTranslations != null) {
                announcement.getTranslations().addAll(newTranslations);
            }
        }

        // --- STOPS ---
        var newStops = stopService.generateStops(dto.stops, announcement);

        // Mutate existing collection safely (Works for both CREATE and UPDATE)
        if (announcement.getStops() == null) {
            announcement.setStops(new ArrayList<>(newStops));
        } else {
            announcement.getStops().clear();
            if (newStops != null) {
                announcement.getStops().addAll(newStops);
            }
        }
    }

    @Transactional
    public void delete(Long id, Long currentUserId) {
        Announcement announcement = announcementRepository.findByIdOptional(id)
                .orElseThrow(() -> new EntityNotFoundException("error.notfound"));

        // Ownership validation
        checkOwnership(currentUserId, announcement);

        // Extract S3 image URLs before deleting the entity
        List<String> photoUrls = announcement.getPhotos().stream()
                .map(Photo::getUrl)
                .collect(Collectors.toList());

        // Delete from database
        announcementRepository.delete(announcement);

        // Delete files from S3
        imageStoreService.deleteAll(photoUrls);
    }

    // -----------------------
    // ENTITY → DTO
    // -----------------------

    // In AnnouncementService.java

    public AnnouncementDTO toAnnouncementDTO(Announcement a, String targetLang) {
        AnnouncementDTO dto = new AnnouncementDTO();

        dto.id = a.getId();
        dto.type = a.getType();
        dto.userId = (a.getUser() != null) ? a.getUser().getId() : null;

        // --- POSTING PLACE ---
        dto.postingPlace = a.getPostingPlace();
        dto.postingLatitude = a.getPostingLatitude();
        dto.postingLongitude = a.getPostingLongitude();

        // --- RECEPTION PLACE ---
        dto.receptionPlace = a.getReceptionPlace();
        dto.receptionLatitude = a.getReceptionLatitude();
        dto.receptionLongitude = a.getReceptionLongitude();

        dto.postingDateTime = a.getPostingDateTime();
        dto.receptionDateTime = a.getReceptionDateTime();

        dto.dimensions = dimensionsService.toDTO(a.getDimensions());
        dto.weight = weightService.toDTO(a.getWeight());

        // --- TRANSLATIONS (Returns single matching language in a list) ---
        AnnouncementTranslationDTO resolvedTranslation =
                announcementTranslationService.toSingleDTO(a.getTranslations(), targetLang);

        dto.translations = (resolvedTranslation != null) ? List.of(resolvedTranslation) : List.of();

        dto.stops = a.getStops().stream()
                .map(stopService::toDTO)
                .toList();

        dto.photos = a.getPhotos().stream()
                .map(photoService::toDTO)
                .toList();

        return dto;
    }

    // Keep single-argument overload for backwards compatibility if needed
    public AnnouncementDTO toAnnouncementDTO(Announcement a) {
        return toAnnouncementDTO(a, "en");
    }

    // -----------------------
    // Pagination
    // -----------------------

    @Transactional
    public PaginationResponse<AnnouncementDTO> getPaginated(int page, int size, String targetLang) {
        List<Announcement> entities = announcementRepository.findPaginated(page, size);
        long total = announcementRepository.countAll();

        List<AnnouncementDTO> dtos = entities.stream()
                .map(entity -> toAnnouncementDTO(entity, targetLang))
                .toList();

        return new PaginationResponse<>(dtos, total, page, size);
    }

    public PaginationResponse<AnnouncementDTO> search(AnnouncementSearchDTO filters, int page, int size) {

        List<Announcement> results = announcementRepository.search(filters, page, size);
        long total = announcementRepository.countSearch(filters);

        List<AnnouncementDTO> dtos = results.stream()
                .map(this::toAnnouncementDTO)
                .toList();

        return new PaginationResponse<>(dtos, total, page, size);
    }
}
