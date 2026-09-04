package org.example.myapp.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import org.example.myapp.dto.BlockUserRequest;
import org.example.myapp.model.User;
import org.example.myapp.model.UserBlock;
import org.example.myapp.repository.UserBlockRepository;
import org.example.myapp.repository.UserRepository;

import java.util.Date;

@ApplicationScoped
public class BlockUserService {

    @Inject
    UserRepository userRepository;

    @Inject
    UserBlockRepository userBlockRepository;

    public void block(BlockUserRequest req) {

        if (req.blockerId.equals(req.blockedId)) {
            throw new WebApplicationException("Cannot block yourself", 400);
        }

        User blocker = userRepository.findById(req.blockerId);
        User blocked = userRepository.findById(req.blockedId);

        if (blocker == null || blocked == null) {
            throw new WebApplicationException("User not found", 404);
        }

        // Check if already blocked
        boolean exists = userBlockRepository.exists(req.blockerId, req.blockedId);
        if (exists) {
            throw new WebApplicationException("User already blocked", 409);
        }

        UserBlock ub = new UserBlock();
        ub.setBlocker(blocker);
        ub.setBlocked(blocked);
        ub.setCreatedAt(new Date());

        userBlockRepository.persist(ub);
    }

    public void unblock(Long blockerId, Long blockedId) {

        UserBlock ub = userBlockRepository.find(
                "blocker.id = ?1 AND blocked.id = ?2",
                blockerId, blockedId
        ).firstResult();

        if (ub == null) {
            throw new WebApplicationException("Block entry not found", 404);
        }

        userBlockRepository.delete(ub);
    }
}
