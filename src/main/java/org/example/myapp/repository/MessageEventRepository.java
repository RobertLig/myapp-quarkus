package org.example.myapp.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.example.myapp.model.MessageEvent;

@ApplicationScoped
public class MessageEventRepository implements PanacheRepository<MessageEvent> {

    @Transactional
    public void deleteByUserId(Long userId) {
        delete("user.id", userId);
    }
}
