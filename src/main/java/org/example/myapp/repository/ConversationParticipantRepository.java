package org.example.myapp.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.example.myapp.model.ConversationParticipant;

import java.util.List;

@ApplicationScoped
public class ConversationParticipantRepository implements PanacheRepository<ConversationParticipant> {

    public List<ConversationParticipant> findByUser(Long userId) {
        return find("user.id", userId).list();
    }
}
