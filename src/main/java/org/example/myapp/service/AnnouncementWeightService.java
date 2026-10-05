package org.example.myapp.service;

import org.example.myapp.model.AnnouncementWeight;
import org.example.myapp.dto.AnnouncementWeightDTO;

import jakarta.enterprise.context.ApplicationScoped;
import java.math.BigDecimal;
import java.math.RoundingMode;

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

        // Round to 2 decimal places
        double roundedValue = roundToTwoDecimals(value);

        return new AnnouncementWeight(roundedValue);
    }

    // -----------------------
    // ENTITY → DTO
    // -----------------------

    public AnnouncementWeightDTO toDTO(AnnouncementWeight w) {
        if (w == null) return null;

        AnnouncementWeightDTO dto = new AnnouncementWeightDTO();
        dto.value = roundToTwoDecimals(w.getValue());
        dto.unit = "metric"; // always metric in DB
        return dto;
    }

    // -----------------------
    // HELPER METHOD
    // -----------------------

    private double roundToTwoDecimals(double val) {
        return BigDecimal.valueOf(val)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }
}