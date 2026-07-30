package org.example.myapp.service;

import org.example.myapp.model.AnnouncementWeight;
import org.example.myapp.dto.AnnouncementWeightDTO;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class AnnouncementWeightService {

    // -----------------------
    // DTO → ENTITY
    // -----------------------

    public AnnouncementWeight toEntity(AnnouncementWeightDTO dto) {

        if (dto == null) return null;

        double value = dto.value;

        // Convert imperial → metric (lb → kg)
        if ("imperial".equalsIgnoreCase(dto.unit)) {
            value = value * 0.45359237;
        }

        return new AnnouncementWeight(value);
    }

    // -----------------------
    // ENTITY → DTO
    // -----------------------

    public AnnouncementWeightDTO toDTO(AnnouncementWeight w) {
        AnnouncementWeightDTO dto = new AnnouncementWeightDTO();
        dto.value = w.getValue();
        dto.unit = "metric"; // always metric in DB
        return dto;
    }
}
