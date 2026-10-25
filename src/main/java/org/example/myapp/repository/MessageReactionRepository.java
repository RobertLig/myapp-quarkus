package org.example.myapp.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.example.myapp.model.MessageReaction;

@ApplicationScoped
public class MessageReactionRepository implements PanacheRepository<MessageReaction> {

    @Transactional
    public void deleteByMessageId(Long messageId) {
        delete("message.id", messageId);
    }
}
