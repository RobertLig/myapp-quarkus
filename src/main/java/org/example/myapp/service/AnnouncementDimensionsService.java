package org.example.myapp.service;

import org.example.myapp.model.AnnouncementDimensions;
import org.example.myapp.repository.AnnouncementDimensionsRepository;
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
}
