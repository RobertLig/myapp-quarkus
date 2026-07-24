package org.example.myapp.service;

import org.example.myapp.model.*;
import org.example.myapp.dto.*;
import org.example.myapp.repository.AnnouncementRepository;
import org.example.myapp.model.translation.AnnouncementTranslation;
import org.example.myapp.validation.AnnouncementValidator;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class AnnouncementService {

    @Inject
    AnnouncementValidator announcementValidator;

    @Inject
    AnnouncementRepository announcementRepository;

    @Inject
    UserService userService;

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
    public Announcement create(Announcement announcement) {
        announcementRepository.persist(announcement);
        return announcement;
    }

    @Transactional
    public Announcement update(Announcement existing, AnnouncementDTO dto) {

        // --- BASIC FIELDS ---
        existing.setType(dto.type);
        existing.setPostingPlace(dto.postingPlace);
        existing.setReceptionPlace(dto.receptionPlace);
        existing.setPostingDateTime(dto.postingDateTime);
        existing.setReceptionDateTime(dto.receptionDateTime);

        // --- USER ---
        if (dto.userId != null) {
            User user = userService.getUserById(dto.userId);
            if (user == null) {
                throw new IllegalArgumentException("User not found");
            }
            existing.setUser(user);
        }

        // --- DIMENSIONS ---
        existing.setDimensions(toAnnouncementDimensionsEntity(dto.dimensions));

        // --- WEIGHT ---
        existing.setWeight(toAnnouncementWeightEntity(dto.weight));

        // --- TRANSLATIONS ---
        // Replace entire list
        List<AnnouncementTranslation> newTranslations = dto.translations.stream()
                .map(this::toAnnouncementTranslationEntity)
                .toList();

        // Set back-reference
        newTranslations.forEach(t -> t.setAnnouncement(existing));

        existing.setTranslations(newTranslations);

        // --- STOPS ---
        List<Stop> newStops = dto.stops.stream()
                .map(this::toStopEntity)
                .toList();

        newStops.forEach(s -> s.setAnnouncement(existing));

        existing.setStops(newStops);

        // --- PHOTOS ---
        // IMPORTANT: do NOT update photos here
        // Photos are managed ONLY via /announcements/{id}/photos endpoints

        return existing;
    }

    @Transactional
    public boolean delete(Long id) {
        return announcementRepository.deleteById(id);
    }

    // -----------------------
    // DTO → ENTITY
    // -----------------------

    public Announcement toAnnouncementEntity(AnnouncementDTO dto) {
        Announcement a = new Announcement();

        a.setId(dto.id);
        a.setType(dto.type);

        if (dto.userId != null) {
            User user = userService.getUserById(dto.userId);
            a.setUser(user);
        }

        a.setDimensions(toAnnouncementDimensionsEntity(dto.dimensions));
        a.setWeight(toAnnouncementWeightEntity(dto.weight));

        a.setPostingPlace(dto.postingPlace);
        a.setReceptionPlace(dto.receptionPlace);

        a.setPostingDateTime(dto.postingDateTime);
        a.setReceptionDateTime(dto.receptionDateTime);

        a.setTranslations(dto.translations.stream()
                .map(this::toAnnouncementTranslationEntity)
                .toList());

        a.setStops(dto.stops.stream()
                .map(this::toStopEntity)
                .toList());

        a.setPhotos(dto.photos.stream()
                .map(this::toPhotoEntity)
                .toList());

        return a;
    }

    // -----------------------
    // ENTITY → DTO
    // -----------------------

    public AnnouncementDTO toAnnouncementDTO(Announcement a) {
        AnnouncementDTO dto = new AnnouncementDTO();

        dto.id = a.getId();
        dto.type = a.getType();
        dto.userId = (a.getUser() != null) ? a.getUser().getId() : null;

        dto.dimensions = toAnnouncementDimensionsDTO(a.getDimensions());
        dto.weight = toAnnouncementWeightDTO(a.getWeight());

        dto.postingPlace = a.getPostingPlace();
        dto.receptionPlace = a.getReceptionPlace();

        dto.postingDateTime = a.getPostingDateTime();
        dto.receptionDateTime = a.getReceptionDateTime();

        dto.translations = a.getTranslations().stream()
                .map(this::toAnnouncementTranslationDTO)
                .toList();

        dto.stops = a.getStops().stream()
                .map(this::toStopDTO)
                .toList();

        dto.photos = a.getPhotos().stream()
                .map(this::toPhotoDTO)
                .toList();

        return dto;
    }

    // -----------------------
    // Dimensions
    // -----------------------

    public AnnouncementDimensionsDTO toAnnouncementDimensionsDTO(AnnouncementDimensions d) {
        AnnouncementDimensionsDTO dto = new AnnouncementDimensionsDTO();
        dto.width = d.getWidth();
        dto.height = d.getHeight();
        dto.length = d.getLength();
        return dto;
    }

    public AnnouncementDimensions toAnnouncementDimensionsEntity(AnnouncementDimensionsDTO dto) {
        AnnouncementDimensions d = new AnnouncementDimensions();
        d.setWidth(dto.width);
        d.setHeight(dto.height);
        d.setLength(dto.length);
        return d;
    }

    // -----------------------
    // Weight
    // -----------------------

    public AnnouncementWeightDTO toAnnouncementWeightDTO(AnnouncementWeight w) {
        AnnouncementWeightDTO dto = new AnnouncementWeightDTO();
        dto.value = w.getValue();
        return dto;
    }

    public AnnouncementWeight toAnnouncementWeightEntity(AnnouncementWeightDTO dto) {
        AnnouncementWeight w = new AnnouncementWeight();
        w.setValue(dto.value);
        return w;
    }

    // -----------------------
    // Translation
    // -----------------------

    public AnnouncementTranslationDTO toAnnouncementTranslationDTO(AnnouncementTranslation t) {
        AnnouncementTranslationDTO dto = new AnnouncementTranslationDTO();
        dto.language = t.getLanguage();
        dto.title = t.getTitle();
        dto.description = t.getDescription();
        return dto;
    }

    public AnnouncementTranslation toAnnouncementTranslationEntity(AnnouncementTranslationDTO dto) {
        AnnouncementTranslation t = new AnnouncementTranslation();
        t.setLanguage(dto.language);
        t.setTitle(dto.title);
        t.setDescription(dto.description);
        return t;
    }

    // -----------------------
    // Stop
    // -----------------------

    public StopDTO toStopDTO(Stop stop) {
        StopDTO dto = new StopDTO();
        dto.id = stop.getId();
        dto.address = stop.getAddress();
        dto.latitude = stop.getLatitude();
        dto.longitude = stop.getLongitude();
        return dto;
    }

    public Stop toStopEntity(StopDTO dto) {
        Stop stop = new Stop();
        stop.setId(dto.id);
        stop.setAddress(dto.address);
        stop.setLatitude(dto.latitude);
        stop.setLongitude(dto.longitude);
        return stop;
    }

    // -----------------------
    // Photo
    // -----------------------

    public PhotoDTO toPhotoDTO(Photo photo) {
        PhotoDTO dto = new PhotoDTO();
        dto.id = photo.getId();
        dto.url = photo.getUrl();
        dto.position = photo.getPosition();
        return dto;
    }

    public Photo toPhotoEntity(PhotoDTO dto) {
        Photo photo = new Photo();
        photo.setId(dto.id);
        photo.setUrl(dto.url);
        photo.setPosition(dto.position);
        return photo;
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
