package org.example.myapp.service;

import org.example.myapp.model.Stop;
import org.example.myapp.repository.StopRepository;
import org.example.myapp.dto.StopDTO;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class StopService {

    @Inject
    StopRepository stopRepository;

    public List<Stop> findAll() {
        return stopRepository.listAll();
    }

    public Optional<Stop> findById(Long id) {
        return stopRepository.findByIdOptional(id);
    }

    @Transactional
    public Stop create(Stop stop) {
        stopRepository.persist(stop);
        return stop;
    }

    @Transactional
    public boolean delete(Long id) {
        return stopRepository.deleteById(id);
    }

    public StopDTO toDTO(Stop stop) {
        StopDTO dto = new StopDTO();
        dto.id = stop.getId();
        dto.address = stop.getAddress();
        dto.latitude = stop.getLatitude();
        dto.longitude = stop.getLongitude();
        return dto;
    }

    public Stop toEntity(StopDTO dto) {
        Stop stop = new Stop();
        stop.setId(dto.id);
        stop.setAddress(dto.address);
        stop.setLatitude(dto.latitude);
        stop.setLongitude(dto.longitude);
        return stop;
    }

}
