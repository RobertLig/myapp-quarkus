package org.example.myapp.service;

import org.example.myapp.model.Courier;
import org.example.myapp.repository.CourierRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class CourierService {

    @Inject
    CourierRepository courierRepository;

    public List<Courier> findAll() {
        return courierRepository.listAll();
    }

    public Optional<Courier> findById(Long id) {
        return courierRepository.findByIdOptional(id);
    }

    @Transactional
    public Courier create(Courier courier) {
        courierRepository.persist(courier);
        return courier;
    }

    @Transactional
    public boolean delete(Long id) {
        return courierRepository.deleteById(id);
    }
}
