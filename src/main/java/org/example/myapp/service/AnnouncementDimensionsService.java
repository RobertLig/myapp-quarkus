package org.example.myapp.service;

import org.example.myapp.model.AnnouncementDimensions;
import org.example.myapp.repository.AnnouncementDimensionsRepository;
import org.example.myapp.dto.AnnouncementDimensionsDTO;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class AnnouncementDimensionsService {

    @Inject
    AnnouncementDimensionsRepository dimensionsRepository;

    public List<AnnouncementDimensions> findAll() {
        return dimensionsRepository.listAll();
    }

    public Optional<AnnouncementDimensions> findById(Long id) {
        return dimensionsRepository.findByIdOptional(id);
    }

    @Transactional
    public AnnouncementDimensions create(AnnouncementDimensions dimensions) {
        dimensionsRepository.persist(dimensions);
        return dimensions;
    }

    @Transactional
    public boolean delete(Long id) {
        return dimensionsRepository.deleteById(id);
    }

    public AnnouncementDimensionsDTO toDTO(AnnouncementDimensions d) {
        AnnouncementDimensionsDTO dto = new AnnouncementDimensionsDTO();
        dto.width = d.getWidth();
        dto.height = d.getHeight();
        dto.length = d.getLength();
        return dto;
    }

    public AnnouncementDimensions toEntity(AnnouncementDimensionsDTO dto) {
        return new AnnouncementDimensions(
                dto.width,
                dto.height,
                dto.length,
                dto.unit
        );
    }
}
