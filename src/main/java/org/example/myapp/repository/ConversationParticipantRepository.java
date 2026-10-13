package org.example.myapp.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Parameters;
import jakarta.enterprise.context.ApplicationScoped;
import org.example.myapp.model.ConversationParticipant;
import java.util.List;

@ApplicationScoped
public class ConversationParticipantRepository implements PanacheRepository<ConversationParticipant> {

    public List<ConversationParticipant> findActiveByUserWithDetails(Long userId) {
        return find(
                "SELECT cp FROM ConversationParticipant cp " +
                        "JOIN FETCH cp.conversation c " +
                        "LEFT JOIN FETCH c.participants p " +
                        "LEFT JOIN FETCH p.user " +
                        "WHERE cp.user.id = :userId AND cp.deletedAt IS NULL",
                Parameters.with("userId", userId)
        ).list();
    }

    public boolean isUserInConversation(Long userId, Long conversationId) {
        long count = find(
                "user.id = :userId and conversation.id = :convId and deletedAt is null",
                Parameters.with("userId", userId).and("convId", conversationId)
        ).count();
        return count > 0;
    }
}