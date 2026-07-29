package org.example.myapp.service;

import org.example.myapp.model.AnnouncementDimensions;
import org.example.myapp.dto.AnnouncementDimensionsDTO;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

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

        return new AnnouncementDimensions(width, height, length);
    }

    // -----------------------
    // ENTITY → DTO
    // -----------------------

    public AnnouncementDimensionsDTO toDTO(AnnouncementDimensions d) {
        AnnouncementDimensionsDTO dto = new AnnouncementDimensionsDTO();
        dto.width = d.getWidth();
        dto.height = d.getHeight();
        dto.length = d.getLength();
        dto.unit = "metric"; // always metric in DB
        return dto;
    }
}
