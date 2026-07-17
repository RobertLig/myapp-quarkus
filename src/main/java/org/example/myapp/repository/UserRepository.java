package org.example.myapp.repository;

import jakarta.enterprise.context.ApplicationScoped;
import org.example.myapp.model.User;
import io.quarkus.hibernate.orm.panache.PanacheRepository;

@ApplicationScoped
public class UserRepository implements PanacheRepository<User> {

    public User findByEmail(String email) {
        return find("email", email).firstResult();
    }

    public boolean existsByEmail(String email) {
        return find("email", email).count() > 0;
    }
}
