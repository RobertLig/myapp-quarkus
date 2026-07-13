package org.example.myapp.repository;

import org.example.myapp.model.Announcement;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class AnnouncementRepository implements PanacheRepository<Announcement> {
}
