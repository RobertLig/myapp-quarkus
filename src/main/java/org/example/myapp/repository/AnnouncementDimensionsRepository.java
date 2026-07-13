package org.example.myapp.repository;

import org.example.myapp.model.AnnouncementDimensions;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class AnnouncementDimensionsRepository implements PanacheRepository<AnnouncementDimensions> {
}
