package org.example.myapp.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.example.myapp.model.Message;

import java.util.Date;
import java.util.List;

@ApplicationScoped
public class MessageRepository implements PanacheRepository<Message> {

    @Transactional
    public void nullifySender(Long userId) {
        update("SET sender = NULL WHERE sender.id = ?1", userId);
    }

    public List<Message> findByConversation(Long conversationId) {
        return find("conversation.id", conversationId).list();
    }

    public List<Message> findPaginated(Long conversationId, Long beforeTimestamp, int limit) {

        if (beforeTimestamp == null) {
            // Initial load: newest messages
            return find("conversation.id = ?1 AND deletedAt IS NULL ORDER BY createdAt DESC", conversationId)
                    .page(0, limit)
                    .list();
        }

        // Load older messages
        return find("conversation.id = ?1 AND deletedAt IS NULL AND createdAt < ?2 ORDER BY createdAt DESC",
                conversationId, new Date(beforeTimestamp))
                .page(0, limit)
                .list();
    }

    public List<Message> search(Long conversationId, String query, Long beforeTimestamp, int limit) {

        if (beforeTimestamp == null) {
            // Initial search: newest matches
            return find("conversation.id = ?1 AND deletedAt IS NULL AND content ILIKE ?2 ORDER BY createdAt DESC",
                    conversationId, "%" + query + "%")
                    .page(0, limit)
                    .list();
        }

        // Search older matches
        return find("conversation.id = ?1 AND deletedAt IS NULL AND content ILIKE ?2 AND createdAt < ?3 ORDER BY createdAt DESC",
                conversationId, "%" + query + "%", new Date(beforeTimestamp))
                .page(0, limit)
                .list();
    }
}
