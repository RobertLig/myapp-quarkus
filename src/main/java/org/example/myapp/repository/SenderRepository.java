package org.example.myapp.repository;

import org.example.myapp.model.Sender;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SenderRepository implements PanacheRepository<Sender> {
}
