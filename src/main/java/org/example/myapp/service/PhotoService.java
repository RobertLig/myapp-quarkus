package org.example.myapp.service;

import org.example.myapp.model.Photo;
import org.example.myapp.repository.PhotoRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class PhotoService {

    @Inject
    PhotoRepository photoRepository;

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
}
