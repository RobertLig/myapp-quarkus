package org.example.myapp.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.example.myapp.model.MessageAttachment;

import java.util.List;

@ApplicationScoped
public class MessageAttachmentRepository implements PanacheRepository<MessageAttachment> {

    public List<MessageAttachment> findByMessageId(Long messageId) {
        return list("message.id", messageId);
    }

    public List<MessageAttachment> findByUserId(Long userId) {
        return list("user.id", userId);
    }

    @Transactional
    public void deleteByUserId(Long userId) {
        delete("user.id", userId);
    }

    @Transactional
    public void deleteByMessageId(Long messageId) {
        delete("message.id", messageId);
    }
}
