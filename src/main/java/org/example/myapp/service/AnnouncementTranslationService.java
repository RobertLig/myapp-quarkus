package org.example.myapp.service;

import jakarta.enterprise.context.ApplicationScoped;
import org.example.myapp.dto.AnnouncementTranslationDTO;
import org.example.myapp.model.Announcement;
import org.example.myapp.model.translation.AnnouncementTranslation;

import java.util.List;
import jakarta.inject.Inject;

@ApplicationScoped
public class AnnouncementTranslationService {

    @Inject
    TranslationService translationService; // LibreTranslate

    // -----------------------
    // DTO → ENTITY
    // -----------------------

    public AnnouncementTranslation toEntity(AnnouncementTranslationDTO dto) {
        if (dto == null) {
            return null;
        }

        AnnouncementTranslation t = new AnnouncementTranslation(
                normalizeLanguage(dto.language),
                dto.title,
                dto.description
        );

        return t;
    }

    // -----------------------
    // ENTITY → DTO
    // -----------------------

    public AnnouncementTranslationDTO toDTO(AnnouncementTranslation t) {
        if (t == null) {
            return null;
        }

        AnnouncementTranslationDTO dto = new AnnouncementTranslationDTO();
        dto.language = t.getLanguage();
        dto.title = t.getTitle();
        dto.description = t.getDescription();
        return dto;
    }

    // -----------------------
    // LIST CONVERSIONS
    // -----------------------

    public List<AnnouncementTranslation> toEntityList(List<AnnouncementTranslationDTO> dtos) {
        return dtos.stream()
                .map(this::toEntity)
                .toList();
    }

    public List<AnnouncementTranslationDTO> toDTOList(List<AnnouncementTranslation> entities) {
        return entities.stream()
                .map(this::toDTO)
                .toList();
    }

    // -----------------------
    // HELPERS
    // -----------------------

    private String normalizeLanguage(String lang) {
        if (lang == null) return null;
        return lang.trim().toLowerCase();
    }

    /**
     * Optional helper: attach translations to announcement.
     * This keeps AnnouncementService cleaner.
     */
    public void attachToAnnouncement(Announcement announcement, List<AnnouncementTranslation> translations) {
        for (AnnouncementTranslation t : translations) {
            t.setAnnouncement(announcement);
        }
        announcement.setTranslations(translations);
    }

    // -----------------------
    // TRANSLATION GENERATION
    // -----------------------

    public List<AnnouncementTranslation> generateTranslations(AnnouncementTranslationDTO original, Announcement announcement) {

        var enTitle = translationService.translate(original.title, "en");
        var enDesc  = translationService.translate(original.description, "en");

        var plTitle = translationService.translate(original.title, "pl");
        var plDesc  = translationService.translate(original.description, "pl");

        AnnouncementTranslation en = new AnnouncementTranslation(
                "en",
                enTitle.text,
                enDesc.text
        );
        en.setAnnouncement(announcement);

        AnnouncementTranslation pl = new AnnouncementTranslation(
                "pl",
                plTitle.text,
                plDesc.text
        );
        pl.setAnnouncement(announcement);

        return List.of(en, pl);
    }
}
