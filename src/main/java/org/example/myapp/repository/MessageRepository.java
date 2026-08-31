package org.example.myapp.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.example.myapp.model.Message;

import java.util.List;

@ApplicationScoped
public class MessageRepository implements PanacheRepository<Message> {

    public List<Message> findByConversation(Long conversationId) {
        return find("conversation.id", conversationId).list();
    }
}
