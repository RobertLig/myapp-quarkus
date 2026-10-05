package org.example.myapp.service;

import org.example.myapp.model.AnnouncementDimensions;
import org.example.myapp.dto.AnnouncementDimensionsDTO;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;

@ApplicationScoped
public class AnnouncementDimensionsService {

    @Inject
    DimensionConversionService dimensionConversionService;

    // -----------------------
    // DTO → ENTITY
    // -----------------------

    public AnnouncementDimensions toEntity(AnnouncementDimensionsDTO dto) {

        if (dto == null) {
            return null;
        }

        double width = dto.width;
        double height = dto.height;
        double length = dto.length;

        // Convert imperial → metric
        if ("imperial".equalsIgnoreCase(dto.unit)) {
            var metric = dimensionConversionService.toMetric(width, height, length);
            width = metric.width;
            height = metric.height;
            length = metric.length;
        }

        // Round all dimensions to 2 decimal places
        width = roundToTwoDecimals(width);
        height = roundToTwoDecimals(height);
        length = roundToTwoDecimals(length);

        return new AnnouncementDimensions(width, height, length);
    }

    // -----------------------
    // ENTITY → DTO
    // -----------------------

    public AnnouncementDimensionsDTO toDTO(AnnouncementDimensions d) {
        if (d == null) {
            return null;
        }

        AnnouncementDimensionsDTO dto = new AnnouncementDimensionsDTO();
        dto.width = roundToTwoDecimals(d.getWidth());
        dto.height = roundToTwoDecimals(d.getHeight());
        dto.length = roundToTwoDecimals(d.getLength());
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