package org.example.myapp.service;

import jakarta.ws.rs.WebApplicationException;
import org.example.myapp.model.*;
import org.example.myapp.dto.*;
import org.example.myapp.repository.AnnouncementRepository;
import org.example.myapp.model.translation.AnnouncementTranslation;
import org.example.myapp.validation.AnnouncementValidator;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class AnnouncementService {

    @Inject
    AnnouncementValidator announcementValidator;

    @Inject
    AnnouncementRepository announcementRepository;

    @Inject
    TranslationService translationService;

    @Inject
    UserService userService;

    @Inject
    AnnouncementDimensionsService dimensionsService;

    @Inject
    DimensionConversionService dimensionConversionService;

    @Inject
    StopService stopService;

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
    public Announcement create(AnnouncementDTO dto) {

        var user = userService.getUserById(dto.userId);
        if (user.isEmpty()) {
            throw new WebApplicationException("error.user.required", 400);
        }

        Announcement announcement = new Announcement(dto.type, user.get());

        applyAnnouncementData(announcement, dto);

        announcementRepository.persist(announcement);
        return announcement;
    }

    @Transactional
    public Announcement update(Announcement existing, AnnouncementDTO dto) {

        applyAnnouncementData(existing, dto);

        return existing;
    }

    private void applyAnnouncementData(Announcement announcement, AnnouncementDTO dto) {

        // --- VALIDATION ---
        var errors = announcementValidator.validate(dto);
        if (!errors.isEmpty()) {
            throw new WebApplicationException(String.join(";", errors), 400);
        }

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
                throw new WebApplicationException("error.user.required", 400);
            }
            announcement.setUser(userOpt.get());
        }

        // --- DIMENSIONS ---
        announcement.setDimensions(dimensionsService.toEntity(dto.dimensions));

        // --- WEIGHT ---
        announcement.setWeight(toAnnouncementWeightEntity(dto.weight));

        // --- TRANSLATIONS ---
        AnnouncementTranslationDTO original = dto.translations.get(0);

        var enTitle = translationService.translate(original.title, "en");
        var enDesc  = translationService.translate(original.description, "en");

        var plTitle = translationService.translate(original.title, "pl");
        var plDesc  = translationService.translate(original.description, "pl");

        AnnouncementTranslation en = new AnnouncementTranslation(
                "en",
                enTitle.text,
                enDesc.text
        );
        en.setAnnouncement(announcement);

        AnnouncementTranslation pl = new AnnouncementTranslation(
                "pl",
                plTitle.text,
                plDesc.text
        );
        pl.setAnnouncement(announcement);

        announcement.setTranslations(List.of(en, pl));

        // --- STOPS ---
        if (dto.stops != null && !dto.stops.isEmpty()) {

            List<Stop> stops = new ArrayList<>();

            for (int i = 0; i < dto.stops.size(); i++) {
                StopDTO stopDTO = dto.stops.get(i);

                if (stopDTO.position == null) {
                    stopDTO.position = i;
                }

                Stop stop = stopService.toEntity(stopDTO);
                stop.setAnnouncement(announcement);
                stops.add(stop);
            }

            stops.sort(Comparator.comparingInt(s -> s.position));
            announcement.setStops(stops);
        }
    }

    @Transactional
    public boolean delete(Long id) {
        return announcementRepository.deleteById(id);
    }

    // -----------------------
    // DTO → ENTITY
    // -----------------------

    public Announcement toAnnouncementEntity(AnnouncementDTO dto) {

        // User must already be loaded before calling this method
        Optional<User> userOpt = userService.getUserById(dto.userId);
        if (userOpt.isEmpty()) {
            throw new WebApplicationException("error.user.required", 400);
        }

        User user = userOpt.get();

        // Use the public constructor
        Announcement a = new Announcement(dto.type, user);

        // Set remaining fields
        a.setId(dto.id);

        // --- POSTING PLACE ---
        a.setPostingPlace(dto.postingPlace);
        a.setPostingLatitude(dto.postingLatitude);
        a.setPostingLongitude(dto.postingLongitude);

        // --- RECEPTION PLACE ---
        a.setReceptionPlace(dto.receptionPlace);
        a.setReceptionLatitude(dto.receptionLatitude);
        a.setReceptionLongitude(dto.receptionLongitude);

        a.setPostingDateTime(dto.postingDateTime);
        a.setReceptionDateTime(dto.receptionDateTime);

        a.setTranslations(dto.translations.stream()
                .map(this::toAnnouncementTranslationEntity)
                .toList());

        if (dto.dimensions != null) {
            a.setDimensions(dimensionsService.toEntity(dto.dimensions));
        }

        if (dto.weight != null) {
            a.setWeight(toAnnouncementWeightEntity(dto.weight));
        }

        if (dto.stops != null) {
            a.setStops(dto.stops.stream()
                    .map(this::toStopEntity)
                    .toList());
        }

        if (dto.photos != null) {
            a.setPhotos(dto.photos.stream()
                    .map(this::toPhotoEntity)
                    .toList());
        }

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
        dto.unit = "metric"; // always stored as metric
        return dto;
    }
    
    // -----------------------
    // Weight
    // -----------------------

    public AnnouncementWeightDTO toAnnouncementWeightDTO(AnnouncementWeight w) {
        AnnouncementWeightDTO dto = new AnnouncementWeightDTO();
        dto.value = w.getValue();
        dto.unit = "metric"; // always metric in DB
        return dto;
    }

    public AnnouncementWeight toAnnouncementWeightEntity(AnnouncementWeightDTO dto) {
        if (dto == null) return null;

        double value = dto.value;

        if ("imperial".equalsIgnoreCase(dto.unit)) {
            value = value * 0.45359237;
        }

        return new AnnouncementWeight(value);
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
        AnnouncementTranslation t = new AnnouncementTranslation(
                dto.language,
                dto.title,
                dto.description
        );
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
        return new Stop(
                dto.id,
                dto.address,
                dto.latitude,
                dto.longitude,
                dto.position
        );
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
        return new Photo(
                dto.id,
                dto.url,
                dto.position
        );
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
