package org.example.myapp.repository;

import org.example.myapp.model.Announcement;
import org.example.myapp.dto.AnnouncementSearchDTO;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.HashMap;
import java.util.List;

@ApplicationScoped
public class AnnouncementRepository implements PanacheRepository<Announcement> {

    // ------------------------------------------------------------
    // Existing methods (KEEP THEM)
    // ------------------------------------------------------------

    public List<Announcement> findPaginated(int page, int size) {
        return findAll().page(page, size).list();
    }

    public long countAll() {
        return count();
    }

    // ------------------------------------------------------------
    // NEW: Search methods (ADD THESE)
    // ------------------------------------------------------------

    public List<Announcement> search(AnnouncementSearchDTO f, int page, int size) {

        var params = new HashMap<String, Object>();
        var jpql = new StringBuilder("SELECT a FROM Announcement a WHERE 1=1");

        // build query + params
        if (f.type != null) {
            jpql.append(" AND a.type = :type");
            params.put("type", f.type);
        }

        if (f.postingPlace != null) {
            jpql.append(" AND a.postingPlace LIKE CONCAT('%', :postingPlace, '%')");
            params.put("postingPlace", f.postingPlace);
        }

        if (f.receptionPlace != null) {
            jpql.append(" AND a.receptionPlace LIKE CONCAT('%', :receptionPlace, '%')");
            params.put("receptionPlace", f.receptionPlace);
        }

        if (f.postingFrom != null) {
            jpql.append(" AND a.postingDateTime >= :postingFrom");
            params.put("postingFrom", f.postingFrom);
        }

        if (f.postingTo != null) {
            jpql.append(" AND a.postingDateTime <= :postingTo");
            params.put("postingTo", f.postingTo);
        }

        if (f.receptionFrom != null) {
            jpql.append(" AND a.receptionDateTime >= :receptionFrom");
            params.put("receptionFrom", f.receptionFrom);
        }

        if (f.receptionTo != null) {
            jpql.append(" AND a.receptionDateTime <= :receptionTo");
            params.put("receptionTo", f.receptionTo);
        }

        // Dimensions
        if (f.minWidth != null) {
            jpql.append(" AND a.dimensions.width >= :minWidth");
            params.put("minWidth", f.minWidth);
        }
        if (f.maxWidth != null) {
            jpql.append(" AND a.dimensions.width <= :maxWidth");
            params.put("maxWidth", f.maxWidth);
        }

        if (f.minHeight != null) {
            jpql.append(" AND a.dimensions.height >= :minHeight");
            params.put("minHeight", f.minHeight);
        }
        if (f.maxHeight != null) {
            jpql.append(" AND a.dimensions.height <= :maxHeight");
            params.put("maxHeight", f.maxHeight);
        }

        if (f.minLength != null) {
            jpql.append(" AND a.dimensions.length >= :minLength");
            params.put("minLength", f.minLength);
        }
        if (f.maxLength != null) {
            jpql.append(" AND a.dimensions.length <= :maxLength");
            params.put("maxLength", f.maxLength);
        }

        // Weight
        if (f.minWeight != null) {
            jpql.append(" AND a.weight.value >= :minWeight");
            params.put("minWeight", f.minWeight);
        }
        if (f.maxWeight != null) {
            jpql.append(" AND a.weight.value <= :maxWeight");
            params.put("maxWeight", f.maxWeight);
        }

        // User filter
        if (f.userId != null) {
            jpql.append(" AND a.user.id = :userId");
            params.put("userId", f.userId);
        }

        var query = getEntityManager()
                .createQuery(jpql.toString(), Announcement.class);

        // apply parameters
        for (var entry : params.entrySet()) {
            query.setParameter(entry.getKey(), entry.getValue());
        }

        return query
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();
    }

    public long countSearch(AnnouncementSearchDTO f) {

        var params = new HashMap<String, Object>();
        var jpql = new StringBuilder("SELECT COUNT(a) FROM Announcement a WHERE 1=1");

        // same filters as in search()
        if (f.type != null) {
            jpql.append(" AND a.type = :type");
            params.put("type", f.type);
        }

        if (f.postingPlace != null) {
            jpql.append(" AND a.postingPlace LIKE CONCAT('%', :postingPlace, '%')");
            params.put("postingPlace", f.postingPlace);
        }

        if (f.receptionPlace != null) {
            jpql.append(" AND a.receptionPlace LIKE CONCAT('%', :receptionPlace, '%')");
            params.put("receptionPlace", f.receptionPlace);
        }

        if (f.postingFrom != null) {
            jpql.append(" AND a.postingDateTime >= :postingFrom");
            params.put("postingFrom", f.postingFrom);
        }

        if (f.postingTo != null) {
            jpql.append(" AND a.postingDateTime <= :postingTo");
            params.put("postingTo", f.postingTo);
        }

        if (f.receptionFrom != null) {
            jpql.append(" AND a.receptionDateTime >= :receptionFrom");
            params.put("receptionFrom", f.receptionFrom);
        }

        if (f.receptionTo != null) {
            jpql.append(" AND a.receptionDateTime <= :receptionTo");
            params.put("receptionTo", f.receptionTo);
        }

        // Dimensions
        if (f.minWidth != null) {
            jpql.append(" AND a.dimensions.width >= :minWidth");
            params.put("minWidth", f.minWidth);
        }
        if (f.maxWidth != null) {
            jpql.append(" AND a.dimensions.width <= :maxWidth");
            params.put("maxWidth", f.maxWidth);
        }

        if (f.minHeight != null) {
            jpql.append(" AND a.dimensions.height >= :minHeight");
            params.put("minHeight", f.minHeight);
        }
        if (f.maxHeight != null) {
            jpql.append(" AND a.dimensions.height <= :maxHeight");
            params.put("maxHeight", f.maxHeight);
        }

        if (f.minLength != null) {
            jpql.append(" AND a.dimensions.length >= :minLength");
            params.put("minLength", f.minLength);
        }
        if (f.maxLength != null) {
            jpql.append(" AND a.dimensions.length <= :maxLength");
            params.put("maxLength", f.maxLength);
        }

        // Weight
        if (f.minWeight != null) {
            jpql.append(" AND a.weight.value >= :minWeight");
            params.put("minWeight", f.minWeight);
        }
        if (f.maxWeight != null) {
            jpql.append(" AND a.weight.value <= :maxWeight");
            params.put("maxWeight", f.maxWeight);
        }

        // User filter
        if (f.userId != null) {
            jpql.append(" AND a.user.id = :userId");
            params.put("userId", f.userId);
        }

        var query = getEntityManager()
                .createQuery(jpql.toString(), Long.class);

        for (var entry : params.entrySet()) {
            query.setParameter(entry.getKey(), entry.getValue());
        }

        return query.getSingleResult();
    }
}
