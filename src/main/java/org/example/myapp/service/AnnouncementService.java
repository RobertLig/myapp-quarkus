package org.example.myapp.service;

import org.example.myapp.exception.EntityNotFoundException;
import org.example.myapp.model.*;
import org.example.myapp.dto.*;
import org.example.myapp.repository.AnnouncementRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;

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
    public Announcement create(AnnouncementDTO dto, String defaultLang) {

        var user = userService.getUserById(dto.userId);
        if (user.isEmpty()) {
            throw new EntityNotFoundException("error.user.notfound");
        }

        Announcement announcement = new Announcement(dto.type, user.get());

        applyAnnouncementData(announcement, dto, defaultLang);

        announcementRepository.persist(announcement);
        return announcement;
    }

    @Transactional
    public Announcement update(Announcement existing, AnnouncementDTO dto) {

        applyAnnouncementData(existing, dto, "en-placeholder");

        return existing;
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
        var translations = announcementTranslationService.generateTranslations(
                (dto.translations != null && !dto.translations.isEmpty()) ? dto.translations.get(0) : null,
                announcement,
                defaultLang
        );
        announcement.setTranslations(translations);

        // --- STOPS ---
        var stops = stopService.generateStops(dto.stops, announcement);
        announcement.setStops(stops);
    }

    @Transactional
    public boolean delete(Long id) {
        return announcementRepository.deleteById(id);
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

    public PaginationResponse<AnnouncementDTO> getPaginated(int page, int size) {
        List<Announcement> entities = announcementRepository.findPaginated(page, size);
        long total = announcementRepository.countAll();

        List<AnnouncementDTO> dtos = entities.stream()
                .map(this::toAnnouncementDTO)
                .toList();

        return new PaginationResponse<>(dtos, total, page, size);
    }

    //ownership
    public boolean isOwner(Long userId, Announcement announcement) {
        return announcement.getUser() != null &&
                announcement.getUser().getId().equals(userId);
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
