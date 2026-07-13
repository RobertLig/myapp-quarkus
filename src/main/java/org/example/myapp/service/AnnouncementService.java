package org.example.myapp.service;

import org.example.myapp.model.Announcement;
import org.example.myapp.repository.AnnouncementRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class AnnouncementService {

    @Inject
    AnnouncementRepository announcementRepository;

    public List<Announcement> findAll() {
        return announcementRepository.listAll();
    }

    public Optional<Announcement> findById(Long id) {
        return announcementRepository.findByIdOptional(id);
    }

    @Transactional
    public Announcement create(Announcement announcement) {
        announcementRepository.persist(announcement);
        return announcement;
    }

    @Transactional
    public boolean delete(Long id) {
        return announcementRepository.deleteById(id);
    }
}
