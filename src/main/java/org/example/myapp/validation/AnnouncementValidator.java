package org.example.myapp.validation;

import jakarta.enterprise.context.ApplicationScoped;
import org.example.myapp.dto.*;

import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class AnnouncementValidator {

    public List<String> validate(AnnouncementDTO dto) {

        List<String> errors = new ArrayList<>();

        validateRequiredFields(dto, errors);
        validateType(dto, errors);
        validatePlaces(dto, errors);
        validateCoordinates(dto, errors);
        validateDates(dto, errors);
        validateDimensions(dto, errors);
        validateWeight(dto, errors);
        validateTranslations(dto, errors);
        validateStops(dto, errors);

        return errors;
    }

    // ------------------------------------------------------------
    // Required scalar fields
    // ------------------------------------------------------------
    private void validateRequiredFields(AnnouncementDTO dto, List<String> errors) {

        if (dto.type == null || dto.type.isBlank()) {
            errors.add("Type is required");
        }

        if (dto.userId == null) {
            errors.add("User ID is required");
        }

        if (dto.postingPlace == null || dto.postingPlace.isBlank()) {
            errors.add("Posting place is required");
        }

        if (dto.receptionPlace == null || dto.receptionPlace.isBlank()) {
            errors.add("Reception place is required");
        }

        if (dto.postingDateTime == null) {
            errors.add("Posting date/time is required");
        }

        if (dto.receptionDateTime == null) {
            errors.add("Reception date/time is required");
        }
    }

    // ------------------------------------------------------------
    // Type validation
    // ------------------------------------------------------------
    private void validateType(AnnouncementDTO dto, List<String> errors) {

        if (dto.type == null) return;

        if (!dto.type.equals("sender") && !dto.type.equals("courier")) {
            errors.add("Type must be either 'sender' or 'courier'");
        }
    }

    // ------------------------------------------------------------
    // Places validation
    // ------------------------------------------------------------
    private void validatePlaces(AnnouncementDTO dto, List<String> errors) {

        if (dto.postingPlace == null || dto.receptionPlace == null) return;

        if (dto.postingPlace.equalsIgnoreCase(dto.receptionPlace)) {
            errors.add("Posting place and reception place cannot be the same");
        }
    }

    // ------------------------------------------------------------
    // Coordinates validation
    // ------------------------------------------------------------
    private void validateCoordinates(AnnouncementDTO dto, List<String> errors) {

        // Posting coordinates
        if (dto.postingLatitude < -90 || dto.postingLatitude > 90) {
            errors.add("Posting latitude must be between -90 and 90");
        }
        if (dto.postingLongitude < -180 || dto.postingLongitude > 180) {
            errors.add("Posting longitude must be between -180 and 180");
        }

        // Reception coordinates
        if (dto.receptionLatitude < -90 || dto.receptionLatitude > 90) {
            errors.add("Reception latitude must be between -90 and 90");
        }
        if (dto.receptionLongitude < -180 || dto.receptionLongitude > 180) {
            errors.add("Reception longitude must be between -180 and 180");
        }

        // Optional: prevent identical coordinates
        if (dto.postingLatitude == dto.receptionLatitude &&
                dto.postingLongitude == dto.receptionLongitude) {
            errors.add("Posting and reception coordinates cannot be identical");
        }
    }

    // ------------------------------------------------------------
    // Dates validation
    // ------------------------------------------------------------
    private void validateDates(AnnouncementDTO dto, List<String> errors) {

        if (dto.postingDateTime == null || dto.receptionDateTime == null) return;

        if (dto.postingDateTime.isAfter(dto.receptionDateTime)) {
            errors.add("Posting date/time must be before reception date/time");
        }
    }

    // ------------------------------------------------------------
    // Dimensions validation (optional)
    // ------------------------------------------------------------
    private void validateDimensions(AnnouncementDTO dto, List<String> errors) {

        if (dto.dimensions == null) return;

        var d = dto.dimensions;

        if (d.width <= 0) {
            errors.add("Width must be positive");
        }
        if (d.height <= 0) {
            errors.add("Height must be positive");
        }
        if (d.length <= 0) {
            errors.add("Length must be positive");
        }

        if (d.unit == null || d.unit.isBlank()) {
            errors.add("Dimensions unit is required");
        } else if (!d.unit.equals("metric") && !d.unit.equals("imperial")) {
            errors.add("Dimensions unit must be 'metric' or 'imperial'");
        }
    }

    // ------------------------------------------------------------
    // Weight validation (optional)
    // ------------------------------------------------------------
    private void validateWeight(AnnouncementDTO dto, List<String> errors) {

        if (dto.weight == null) return;

        var w = dto.weight;

        if (w.value <= 0) {
            errors.add("Weight must be positive");
        }

        if (w.unit == null || w.unit.isBlank()) {
            errors.add("Weight unit is required");
        } else if (!w.unit.equals("metric") && !w.unit.equals("imperial")) {
            errors.add("Weight unit must be 'metric' or 'imperial'");
        }
    }


    // ------------------------------------------------------------
    // Translations validation (required)
    // ------------------------------------------------------------
    private void validateTranslations(AnnouncementDTO dto, List<String> errors) {

        if (dto.translations == null || dto.translations.isEmpty()) {
            errors.add("At least one translation is required");
            return;
        }

        if (dto.translations.size() != 1) {
            errors.add("Only one translation should be submitted. Other languages will be generated automatically.");
            return;
        }

        var t = dto.translations.get(0);

        if (t.language == null || t.language.isBlank()) {
            errors.add("Translation language is required");
        }

        if (!t.language.equals("en") && !t.language.equals("pl")) {
            errors.add("Translation language must be 'en' or 'pl'");
        }

        if (t.title == null || t.title.isBlank()) {
            errors.add("Translation title is required");
        }
    }

    private void validateStops(AnnouncementDTO dto, List<String> errors) {

        if (dto.stops == null) return; // stops are optional

        for (int i = 0; i < dto.stops.size(); i++) {
            StopDTO s = dto.stops.get(i);

            if (s.address == null || s.address.isBlank()) {
                errors.add("Stop " + (i + 1) + ": address is required");
            }

            if (s.latitude == null) {
                errors.add("Stop " + (i + 1) + ": latitude is required");
            } else if (s.latitude < -90 || s.latitude > 90) {
                errors.add("Stop " + (i + 1) + ": latitude must be between -90 and 90");
            }

            if (s.longitude == null) {
                errors.add("Stop " + (i + 1) + ": longitude is required");
            } else if (s.longitude < -180 || s.longitude > 180) {
                errors.add("Stop " + (i + 1) + ": longitude must be between -180 and 180");
            }
        }
    }
}
