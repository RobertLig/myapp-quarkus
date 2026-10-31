package org.example.myapp.service;

import org.example.myapp.dto.AnnouncementSearchDTO;
import org.example.myapp.repository.AnnouncementRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AnnouncementServiceSearchTest {

    @Test
    void searchUsesPolishForPolishLocaleRegardlessOfRequestLanguage() {
        assertSearchLanguage("pl", "en", "pl");
    }

    @Test
    void searchDefaultsToEnglishForUnsupportedLocaleRegardlessOfRequestLanguage() {
        assertSearchLanguage("fr", "pl", "en");
    }

    private void assertSearchLanguage(String targetLang, String requestedLang, String expectedLang) {
        AnnouncementRepository repository = mock(AnnouncementRepository.class);
        when(repository.search(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.anyInt(),
                org.mockito.ArgumentMatchers.anyInt())).thenReturn(List.of());
        when(repository.countSearch(org.mockito.ArgumentMatchers.any())).thenReturn(0L);

        AnnouncementService service = new AnnouncementService();
        service.announcementRepository = repository;
        AnnouncementSearchDTO filters = new AnnouncementSearchDTO();
        filters.text = "parcel";
        filters.language = requestedLang;

        service.search(filters, 0, 10, targetLang);

        assertEquals(expectedLang, filters.language);
    }
}
