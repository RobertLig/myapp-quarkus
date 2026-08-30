package org.example.myapp.repository;

import jakarta.enterprise.context.ApplicationScoped;
import org.example.myapp.model.UserBlock;
import io.quarkus.hibernate.orm.panache.PanacheRepository;

@ApplicationScoped
public class UserBlockRepository implements PanacheRepository<UserBlock> {

    public boolean exists(Long blockerId, Long blockedId) {
        return find("blocker.id = ?1 AND blocked.id = ?2", blockerId, blockedId)
                .count() > 0;
    }
}
