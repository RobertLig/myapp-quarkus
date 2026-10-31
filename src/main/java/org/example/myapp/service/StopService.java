package org.example.myapp.service;

import org.example.myapp.model.Stop;
import org.example.myapp.model.Announcement;
import org.example.myapp.dto.StopDTO;
import org.example.myapp.exception.FieldValidationException;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

@ApplicationScoped
public class StopService {

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

    public void synchronizeStops(Announcement announcement, List<Stop> requestedStops) {
        List<Stop> current = announcement.getStops();
        if (current == null) {
            current = new ArrayList<>();
            announcement.setStops(current);
        }

        List<Stop> pending = new ArrayList<>(requestedStops);
        for (Iterator<Stop> iterator = current.iterator(); iterator.hasNext();) {
            Stop existing = iterator.next();
            Stop replacement = pending.stream()
                    .filter(stop -> stop.getId() != null && stop.getId().equals(existing.getId()))
                    .findFirst()
                    .orElse(null);

            if (replacement == null) {
                iterator.remove();
                continue;
            }

            pending.remove(replacement);
            existing.setAddress(replacement.getAddress());
            existing.setLatitude(replacement.getLatitude());
            existing.setLongitude(replacement.getLongitude());
            existing.setPosition(replacement.getPosition());
        }

        for (Stop stop : pending) {
            if (stop.getId() != null) {
                throw new FieldValidationException("stops", "stop.mismatch");
            }
            stop.setAnnouncement(announcement);
            current.add(stop);
        }
    }
}
