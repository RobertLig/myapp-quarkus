package org.example.myapp.service;

import org.example.myapp.model.Stop;
import org.example.myapp.repository.StopRepository;
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
}
