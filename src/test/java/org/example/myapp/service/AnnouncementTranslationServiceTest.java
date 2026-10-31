package org.example.myapp.service;

import org.example.myapp.model.Announcement;
import org.example.myapp.model.translation.AnnouncementTranslation;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class AnnouncementTranslationServiceTest {

    @Test
    void synchronizeTranslationsUpdatesExistingEntitiesInPlace() {
        Announcement announcement = new Announcement("courier", null);
        AnnouncementTranslation english = new AnnouncementTranslation("en", "Old title", "Old description");
        AnnouncementTranslation polish = new AnnouncementTranslation("pl", "Stary tytuł", "Stary opis");
        english.setAnnouncement(announcement);
        polish.setAnnouncement(announcement);
        announcement.getTranslations().addAll(List.of(english, polish));

        new AnnouncementTranslationService().synchronizeTranslations(announcement, List.of(
                new AnnouncementTranslation("en", "New title", "New description"),
                new AnnouncementTranslation("pl", "Nowy tytuł", "Nowy opis")
        ));

        assertEquals(2, announcement.getTranslations().size());
        assertSame(english, announcement.getTranslations().get(0));
        assertSame(polish, announcement.getTranslations().get(1));
        assertEquals("New title", english.getTitle());
        assertEquals("New description", english.getDescription());
        assertEquals("Nowy tytuł", polish.getTitle());
        assertEquals("Nowy opis", polish.getDescription());
    }
}
