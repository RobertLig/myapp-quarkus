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
    // Existing methods
    // ------------------------------------------------------------

    public List<Announcement> findPaginated(int page, int size) {
        return findAll().page(page, size).list();
    }

    public long countAll() {
        return count();
    }

    // ------------------------------------------------------------
    // Search
    // ------------------------------------------------------------

    public List<Announcement> search(AnnouncementSearchDTO f, int page, int size) {

        var params = new HashMap<String, Object>();
        var jpql = new StringBuilder("SELECT a FROM Announcement a WHERE 1=1");

        buildFilters(f, jpql, params);
        buildSorting(f, jpql);

        var query = getEntityManager()
                .createQuery(jpql.toString(), Announcement.class);

        params.forEach(query::setParameter);

        return query
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();
    }

    public long countSearch(AnnouncementSearchDTO f) {

        var params = new HashMap<String, Object>();
        var jpql = new StringBuilder("SELECT COUNT(a) FROM Announcement a WHERE 1=1");

        buildFilters(f, jpql, params);

        var query = getEntityManager()
                .createQuery(jpql.toString(), Long.class);

        params.forEach(query::setParameter);

        return query.getSingleResult();
    }

    // ------------------------------------------------------------
    // Shared filter builder
    // ------------------------------------------------------------

    private void buildFilters(AnnouncementSearchDTO f, StringBuilder jpql, HashMap<String, Object> params) {

        // Type
        if (f.type != null) {
            jpql.append(" AND a.type = :type");
            params.put("type", f.type);
        }

        // Posting place name
        if (f.postingPlace != null) {
            jpql.append(" AND a.postingPlace LIKE CONCAT('%', :postingPlace, '%')");
            params.put("postingPlace", f.postingPlace);
        }

        // Reception place name
        if (f.receptionPlace != null) {
            jpql.append(" AND a.receptionPlace LIKE CONCAT('%', :receptionPlace, '%')");
            params.put("receptionPlace", f.receptionPlace);
        }

        // Posting coordinates
        if (f.postingLatitude != null) {
            jpql.append(" AND a.postingLatitude = :postingLatitude");
            params.put("postingLatitude", f.postingLatitude);
        }
        if (f.postingLongitude != null) {
            jpql.append(" AND a.postingLongitude = :postingLongitude");
            params.put("postingLongitude", f.postingLongitude);
        }

        // Reception coordinates
        if (f.receptionLatitude != null) {
            jpql.append(" AND a.receptionLatitude = :receptionLatitude");
            params.put("receptionLatitude", f.receptionLatitude);
        }
        if (f.receptionLongitude != null) {
            jpql.append(" AND a.receptionLongitude = :receptionLongitude");
            params.put("receptionLongitude", f.receptionLongitude);
        }

        // Radius search (Haversine)
        if (f.radiusKm != null && f.postingLatitude != null && f.postingLongitude != null) {

            jpql.append("""
                AND (
                    6371 * acos(
                        cos(radians(:centerLat)) *
                        cos(radians(a.postingLatitude)) *
                        cos(radians(a.postingLongitude) - radians(:centerLon)) +
                        sin(radians(:centerLat)) *
                        sin(radians(a.postingLatitude))
                    )
                ) <= :radiusKm
            """);

            params.put("centerLat", f.postingLatitude);
            params.put("centerLon", f.postingLongitude);
            params.put("radiusKm", f.radiusKm);
        }

        // Posting date
        if (f.postingFrom != null) {
            jpql.append(" AND a.postingDateTime >= :postingFrom");
            params.put("postingFrom", f.postingFrom);
        }
        if (f.postingTo != null) {
            jpql.append(" AND a.postingDateTime <= :postingTo");
            params.put("postingTo", f.postingTo);
        }

        // Reception date
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

        // Text search in translations
        if (f.text != null && !f.text.isBlank()) {

            jpql.append(" AND EXISTS (");
            jpql.append("   SELECT 1 FROM AnnouncementTranslation t");
            jpql.append("   WHERE t.announcement = a");

            if (f.language != null) {
                jpql.append("     AND t.language = :lang");
                params.put("lang", f.language);
            }

            jpql.append("     AND (t.title LIKE :text OR t.description LIKE :text)");
            jpql.append(")");

            params.put("text", "%" + f.text + "%");
        }
    }

    // ------------------------------------------------------------
    // Sorting
    // ------------------------------------------------------------

    private void buildSorting(AnnouncementSearchDTO f, StringBuilder jpql) {

        if (f.sortBy == null) return;

        jpql.append(" ORDER BY ");

        switch (f.sortBy) {

            case "postingDate" -> jpql.append("a.postingDateTime");
            case "receptionDate" -> jpql.append("a.receptionDateTime");

            case "weight" -> jpql.append("a.weight.value");

            case "width" -> jpql.append("a.dimensions.width");
            case "height" -> jpql.append("a.dimensions.height");
            case "length" -> jpql.append("a.dimensions.length");

            case "postingPlace" -> jpql.append("a.postingPlace");
            case "receptionPlace" -> jpql.append("a.receptionPlace");

            case "type" -> jpql.append("a.type");
            case "user" -> jpql.append("a.user.id");

            default -> jpql.append("a.id");
        }

        if ("desc".equalsIgnoreCase(f.sortDir)) {
            jpql.append(" DESC");
        } else {
            jpql.append(" ASC");
        }
    }
}
