package org.example.myapp.repository;

import org.example.myapp.model.Courier;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CourierRepository implements PanacheRepository<Courier> {
}
