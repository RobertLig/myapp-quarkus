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

    public AnnouncementWeightDTO toDTO(AnnouncementWeight w) {
        AnnouncementWeightDTO dto = new AnnouncementWeightDTO();
        dto.value = w.getValue();
        return dto;
    }

    public AnnouncementWeight toEntity(AnnouncementWeightDTO dto) {
        AnnouncementWeight w = new AnnouncementWeight();
        w.setValue(dto.value);
        return w;
    }
}
