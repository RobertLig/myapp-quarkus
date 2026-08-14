package org.example.myapp.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.example.myapp.model.Conversation;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class ConversationRepository implements PanacheRepository<Conversation> {

    public Optional<Conversation> findExistingConversation(List<Long> userIds) {
        // Find conversations where ALL participants match the provided list
        return find("""
            SELECT c FROM Conversation c
            JOIN c.participants p
            WHERE p.user.id IN ?1
            GROUP BY c.id
            HAVING COUNT(p) = ?2
        """, userIds, userIds.size()).firstResultOptional();
    }
}
