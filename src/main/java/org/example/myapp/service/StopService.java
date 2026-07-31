package org.example.myapp.service;

import org.example.myapp.model.Stop;
import org.example.myapp.model.Announcement;
import org.example.myapp.repository.StopRepository;
import org.example.myapp.dto.StopDTO;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class StopService {

    @Inject
    StopRepository stopRepository;

    // -----------------------
    // CRUD
    // -----------------------

    public List<Stop> findAll() {
        return stopRepository.listAll();
    }

    public Optional<Stop> findById(Long id) {
        return stopRepository.findByIdOptional(id);
    }

    @Transactional
    public Stop create(Stop stop) {
        stopRepository.persist(stop);
        return stop;
    }

    @Transactional
    public boolean delete(Long id) {
        return stopRepository.deleteById(id);
    }

    // -----------------------
    // DTO → ENTITY
    // -----------------------

    public Stop toEntity(StopDTO dto) {
        if (dto == null) { return null; }

        return new Stop(
                dto.id,
                dto.address,
                dto.latitude,
                dto.longitude,
                dto.position != null ? dto.position : 0
        );
    }

    // -----------------------
    // ENTITY → DTO
    // -----------------------

    public StopDTO toDTO(Stop stop) {
        StopDTO dto = new StopDTO();
        dto.id = stop.getId();
        dto.address = stop.getAddress();
        dto.latitude = stop.getLatitude();
        dto.longitude = stop.getLongitude();
        return dto;
    }

    // -----------------------
    // STOP GENERATION LOGIC
    // -----------------------

    public List<Stop> generateStops(List<StopDTO> stopDTOs, Announcement announcement) {

        if (stopDTOs == null || stopDTOs.isEmpty()) {
            return List.of();
        }

        List<Stop> stops = new ArrayList<>();

        for (int i = 0; i < stopDTOs.size(); i++) {
            StopDTO dto = stopDTOs.get(i);

            // Assign default position if missing
            if (dto.position == null) {
                dto.position = i;
            }

            Stop stop = toEntity(dto);
            stop.setAnnouncement(announcement);
            stops.add(stop);
        }

        // Sort by position
        stops.sort(Comparator.comparingInt(Stop::getPosition));

        return stops;
    }
}
