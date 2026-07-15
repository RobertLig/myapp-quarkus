package org.example.myapp.service;

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
    public boolean delete(Long id) {
        return announcementRepository.deleteById(id);
    }

    // -----------------------
    // DTO MAPPERS
    // -----------------------

    // Announcement
    public AnnouncementDTO toDTO(Announcement a) {
        AnnouncementDTO dto = new AnnouncementDTO();
        dto.id = a.getId();
        dto.title = a.getTitle();
        dto.description = a.getDescription();

        dto.dimensions = toDTO(a.getDimensions());
        dto.weight = toDTO(a.getWeight());

        dto.postingPlace = a.getPostingPlace();
        dto.receptionPlace = a.getReceptionPlace();

        dto.postingDateTime = a.getPostingDateTime();
        dto.receptionDateTime = a.getReceptionDateTime();

        dto.sender = toDTO(a.getSender());
        dto.courier = toDTO(a.getCourier());

        dto.stops = a.getStops().stream()
                .map(this::toDTO)
                .toList();

        dto.photos = a.getPhotos().stream()
                .map(this::toDTO)
                .toList();

        return dto;
    }

    public Announcement toEntity(AnnouncementDTO dto) {
        Announcement a = new Announcement();
        a.setId(dto.id);
        a.setTitle(dto.title);
        a.setDescription(dto.description);

        a.setDimensions(toEntity(dto.dimensions));
        a.setWeight(toEntity(dto.weight));

        a.setPostingPlace(dto.postingPlace);
        a.setReceptionPlace(dto.receptionPlace);

        a.setPostingDateTime(dto.postingDateTime);
        a.setReceptionDateTime(dto.receptionDateTime);

        a.setSender(toEntity(dto.sender));
        a.setCourier(toEntity(dto.courier));

        a.setStops(dto.stops.stream()
                .map(this::toEntity)
                .toList());

        a.setPhotos(dto.photos.stream()
                .map(this::toEntity)
                .toList());

        return a;
    }

    // AnnouncementDimensions
    public AnnouncementDimensionsDTO toDTO(AnnouncementDimensions d) {
        AnnouncementDimensionsDTO dto = new AnnouncementDimensionsDTO();
        dto.width = d.getWidth();
        dto.height = d.getHeight();
        dto.length = d.getLength();
        dto.unit = d.getUnit();
        return dto;
    }

    public AnnouncementDimensions toEntity(AnnouncementDimensionsDTO dto) {
        AnnouncementDimensions d = new AnnouncementDimensions();
        d.setWidth(dto.width);
        d.setHeight(dto.height);
        d.setLength(dto.length);
        d.setUnit(dto.unit);
        return d;
    }

    // AnnouncementWeight
    public AnnouncementWeightDTO toDTO(AnnouncementWeight w) {
        AnnouncementWeightDTO dto = new AnnouncementWeightDTO();
        dto.value = w.getValue();
        dto.unit = w.getUnit();
        return dto;
    }

    public AnnouncementWeight toEntity(AnnouncementWeightDTO dto) {
        AnnouncementWeight w = new AnnouncementWeight();
        w.setValue(dto.value);
        w.setUnit(dto.unit);
        return w;
    }

    // Sender
    public SenderDTO toDTO(Sender s) {
        SenderDTO dto = new SenderDTO();
        dto.id = s.getId();
        dto.name = s.getName();
        dto.phone = s.getPhone();
        dto.email = s.getEmail();
        return dto;
    }

    public Sender toEntity(SenderDTO dto) {
        Sender s = new Sender();
        s.setId(dto.id);
        s.setName(dto.name);
        s.setPhone(dto.phone);
        s.setEmail(dto.email);
        return s;
    }

    // Courier
    public CourierDTO toDTO(Courier c) {
        CourierDTO dto = new CourierDTO();
        dto.id = c.getId();
        dto.name = c.getName();
        dto.phone = c.getPhone();
        return dto;
    }

    public Courier toEntity(CourierDTO dto) {
        Courier c = new Courier();
        c.setId(dto.id);
        c.setName(dto.name);
        c.setPhone(dto.phone);
        return c;
    }

    // Stop
    public StopDTO toDTO(Stop stop) {
        StopDTO dto = new StopDTO();
        dto.id = stop.getId();
        dto.address = stop.getAddress();
        dto.latitude = stop.getLatitude();
        dto.longitude = stop.getLongitude();
        return dto;
    }

    public Stop toEntity(StopDTO dto) {
        Stop stop = new Stop();
        stop.setId(dto.id);
        stop.setAddress(dto.address);
        stop.setLatitude(dto.latitude);
        stop.setLongitude(dto.longitude);
        return stop;
    }

    // Photo
    public PhotoDTO toDTO(Photo photo) {
        PhotoDTO dto = new PhotoDTO();
        dto.id = photo.getId();
        dto.url = photo.getUrl();
        return dto;
    }

    public Photo toEntity(PhotoDTO dto) {
        Photo photo = new Photo();
        photo.setId(dto.id);
        photo.setUrl(dto.url);
        return photo;
    }

    public PaginationResponse<AnnouncementDTO> getPaginated(int page, int size) {
        List<Announcement> entities = announcementRepository.findPaginated(page, size);
        long total = announcementRepository.countAll();

        List<AnnouncementDTO> dtos = entities.stream()
                .map(this::toDTO)
                .toList();

        return new PaginationResponse<>(dtos, total, page, size);
    }

}
