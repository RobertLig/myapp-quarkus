package org.example.myapp.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.example.myapp.dto.BlockUserRequest;
import org.example.myapp.exception.DomainException;
import org.example.myapp.exception.EntityNotFoundException;
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

    @Transactional
    public void block(Long blockerId, BlockUserRequest req) {

        if (blockerId.equals(req.blockedId)) {
            throw DomainException.forbidden("error.block.self");
        }

        User blocker = userRepository.findById(blockerId);
        User blocked = userRepository.findById(req.blockedId);

        if (blocker == null || blocked == null) {
            throw new EntityNotFoundException("error.user.notfound");
        }

        // Check if already blocked
        boolean exists = userBlockRepository.exists(blockerId, req.blockedId);
        if (exists) {
            throw DomainException.badRequest("error.block.exists");
        }

        UserBlock ub = new UserBlock();
        ub.setBlocker(blocker);
        ub.setBlocked(blocked);
        ub.setCreatedAt(new Date());

        userBlockRepository.persist(ub);
    }

    @Transactional
    public void unblock(Long blockerId, BlockUserRequest req) {

        UserBlock ub = userBlockRepository.find(
                "blocker.id = ?1 AND blocked.id = ?2",
                blockerId, req.blockedId
        ).firstResult();

        if (ub == null) {
            throw new EntityNotFoundException("error.block.notfound");
        }

        userBlockRepository.delete(ub);
    }
}
