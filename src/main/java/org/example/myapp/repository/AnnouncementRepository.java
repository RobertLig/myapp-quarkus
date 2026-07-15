package org.example.myapp.repository;

import org.example.myapp.model.Announcement;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class AnnouncementRepository implements PanacheRepository<Announcement> {

    public List<Announcement> findPaginated(int page, int size) {
        return findAll().page(page, size).list();
    }

    public long countAll() {
        return count();
    }
}
