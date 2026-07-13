package org.example.myapp.repository;

import org.example.myapp.model.Stop;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class StopRepository implements PanacheRepository<Stop> {
}
