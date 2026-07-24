package org.example.myapp.service;

import org.example.myapp.model.Photo;
import org.example.myapp.repository.PhotoRepository;
import org.example.myapp.dto.PhotoDTO;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class PhotoService {

    @Inject
    PhotoRepository photoRepository;

    // -----------------------
    // CRUD
    // -----------------------

    public List<Photo> findAll() {
        return photoRepository.listAll();
    }

    public Optional<Photo> findById(Long id) {
        return photoRepository.findByIdOptional(id);
    }

    @Transactional
    public Photo create(Photo photo) {
        photoRepository.persist(photo);
        return photo;
    }

    @Transactional
    public boolean delete(Long id) {
        return photoRepository.deleteById(id);
    }

    // -----------------------
    // DTO → ENTITY
    // -----------------------

    public Photo toEntity(PhotoDTO dto) {
        return new Photo(
                dto.id,
                dto.url,
                dto.position
        );
    }

    // -----------------------
    // ENTITY → DTO
    // -----------------------

    public PhotoDTO toDTO(Photo photo) {
        PhotoDTO dto = new PhotoDTO();
        dto.id = photo.getId();
        dto.url = photo.getUrl();
        dto.position = photo.getPosition();
        return dto;
    }

    // -----------------------
    // UPDATE
    // -----------------------

    @Transactional
    public Photo update(Photo photo) {
        // Panache automatically updates managed entities
        return photo;
    }
}
