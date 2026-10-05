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

    public List<AnnouncementTranslation> generateTranslations(
            AnnouncementTranslationDTO original,
            Announcement announcement,
            String defaultLang) {

        // 1. Check if original language is explicitly sent in JSON
        String srcLang = null;
        if (original != null && original.language != null && !original.language.isBlank()) {
            srcLang = original.language.toLowerCase().trim();
        } else {
            srcLang = defaultLang; // Fall back to Accept-Language header
        }

        String title = (original != null && original.title != null) ? original.title : "";
        String description = (original != null && original.description != null) ? original.description : "";

        // 2. Translate to English using resolved source language
        var enTitle = translationService.translate(title, srcLang, "en");
        var enDesc  = translationService.translate(description, srcLang, "en");

        // 3. Translate to Polish using resolved source language
        var plTitle = translationService.translate(title, srcLang, "pl");
        var plDesc  = translationService.translate(description, srcLang, "pl");

        AnnouncementTranslation en = new AnnouncementTranslation("en", enTitle.text, enDesc.text);
        en.setAnnouncement(announcement);

        AnnouncementTranslation pl = new AnnouncementTranslation("pl", plTitle.text, plDesc.text);
        pl.setAnnouncement(announcement);

        return List.of(en, pl);
    }

    /**
     * Returns a single matching translation based on target language,
     * with fallback to 'en', or first available.
     */
    public AnnouncementTranslationDTO toSingleDTO(List<AnnouncementTranslation> translations, String targetLang) {
        if (translations == null || translations.isEmpty()) {
            return null;
        }

        String lang = (targetLang != null && !targetLang.isBlank()) ? targetLang.toLowerCase().trim() : "en";

        // 1. Exact match
        AnnouncementTranslation match = translations.stream()
                .filter(t -> t.getLanguage() != null && t.getLanguage().equalsIgnoreCase(lang))
                .findFirst()
                // 2. Fallback to English
                .orElseGet(() -> translations.stream()
                        .filter(t -> t.getLanguage() != null && t.getLanguage().equalsIgnoreCase("en"))
                        .findFirst()
                        // 3. Fallback to first available entry
                        .orElse(translations.get(0))
                );

        return toDTO(match);
    }
}
