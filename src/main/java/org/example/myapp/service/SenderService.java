package org.example.myapp.service;

import org.example.myapp.model.Sender;
import org.example.myapp.repository.SenderRepository;
import org.example.myapp.dto.SenderDTO;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class SenderService {

    @Inject
    SenderRepository senderRepository;

    public List<Sender> findAll() {
        return senderRepository.listAll();
    }

    public Optional<Sender> findById(Long id) {
        return senderRepository.findByIdOptional(id);
    }

    @Transactional
    public Sender create(Sender sender) {
        senderRepository.persist(sender);
        return sender;
    }

    @Transactional
    public boolean delete(Long id) {
        return senderRepository.deleteById(id);
    }

    public SenderDTO toDTO(Sender sender) {
        SenderDTO dto = new SenderDTO();
        dto.id = sender.getId();
        dto.name = sender.getName();
        dto.phone = sender.getPhone();
        dto.email = sender.getEmail();
        return dto;
    }

    public Sender toEntity(SenderDTO dto) {
        Sender sender = new Sender();
        sender.setId(dto.id);
        sender.setName(dto.name);
        sender.setPhone(dto.phone);
        sender.setEmail(dto.email);
        return sender;
    }
}
