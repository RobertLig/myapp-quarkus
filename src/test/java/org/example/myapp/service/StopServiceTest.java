package org.example.myapp.service;

import org.example.myapp.exception.FieldValidationException;
import org.example.myapp.model.Announcement;
import org.example.myapp.model.Stop;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StopServiceTest {

    private final StopService service = new StopService();

    @Test
    void synchronizeStopsUpdatesManagedStopsAndAddsNewOnes() {
        Announcement announcement = new Announcement("courier", null);
        Stop existing = new Stop(10L, "Old stop", 1, 2, 0);
        existing.setAnnouncement(announcement);
        announcement.getStops().add(existing);

        Stop updated = new Stop(10L, "Updated stop", 3, 4, 1);
        Stop added = new Stop(null, "New stop", 5, 6, 0);
        service.synchronizeStops(announcement, List.of(updated, added));

        assertEquals(2, announcement.getStops().size());
        assertSame(existing, announcement.getStops().get(0));
        assertEquals("Updated stop", existing.getAddress());
        assertEquals(3, existing.getLatitude());
        assertEquals(4, existing.getLongitude());
        assertEquals(1, existing.getPosition());
        assertSame(announcement, added.getAnnouncement());
        assertSame(added, announcement.getStops().get(1));
    }

    @Test
    void synchronizeStopsRejectsIdsNotOwnedByTheRoute() {
        Announcement announcement = new Announcement("courier", null);
        assertThrows(FieldValidationException.class, () -> service.synchronizeStops(
                announcement,
                List.of(new Stop(99L, "Unknown stop", 1, 2, 0))
        ));
    }
}
