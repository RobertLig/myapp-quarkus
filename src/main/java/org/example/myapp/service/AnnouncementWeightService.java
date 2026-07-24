package org.example.myapp.service;

import org.example.myapp.model.AnnouncementWeight;
import org.example.myapp.repository.AnnouncementWeightRepository;
import org.example.myapp.dto.AnnouncementWeightDTO;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class AnnouncementWeightService {

    @Inject
    AnnouncementWeightRepository weightRepository;

    // -----------------------
    // CRUD
    // -----------------------

    public List<AnnouncementWeight> findAll() {
        return weightRepository.listAll();
    }

    public Optional<AnnouncementWeight> findById(Long id) {
        return weightRepository.findByIdOptional(id);
    }

    @Transactional
    public AnnouncementWeight create(AnnouncementWeight weight) {
        weightRepository.persist(weight);
        return weight;
    }

    @Transactional
    public boolean delete(Long id) {
        return weightRepository.deleteById(id);
    }

    // -----------------------
    // DTO → ENTITY
    // -----------------------

    public AnnouncementWeight toEntity(AnnouncementWeightDTO dto) {

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
