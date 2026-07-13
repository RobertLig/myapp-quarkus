package org.example.myapp.repository;

import org.example.myapp.model.AnnouncementWeight;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class AnnouncementWeightRepository implements PanacheRepository<AnnouncementWeight> {
}
