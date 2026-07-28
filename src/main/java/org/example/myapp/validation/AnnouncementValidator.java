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
            errors.add("error.type.required");
        }

        if (dto.userId == null) {
            errors.add("error.user.required");
        }

        if (dto.postingPlace == null || dto.postingPlace.isBlank()) {
            errors.add("error.postingPlace.required");
        }

        if (dto.receptionPlace == null || dto.receptionPlace.isBlank()) {
            errors.add("error.receptionPlace.required");
        }

        if (dto.postingDateTime == null) {
            errors.add("error.postingDateTime.required");
        }

        if (dto.receptionDateTime == null) {
            errors.add("error.receptionDateTime.required");
        }
    }

    // ------------------------------------------------------------
    // Type validation
    // ------------------------------------------------------------
    private void validateType(AnnouncementDTO dto, List<String> errors) {

        if (dto.type == null) return;

        if (!dto.type.equals("sender") && !dto.type.equals("courier")) {
            errors.add("error.type.invalid");
        }
    }

    // ------------------------------------------------------------
    // Places validation
    // ------------------------------------------------------------
    private void validatePlaces(AnnouncementDTO dto, List<String> errors) {

        if (dto.postingPlace == null || dto.receptionPlace == null) return;

        if (dto.postingPlace.equalsIgnoreCase(dto.receptionPlace)) {
            errors.add("error.place.same");
        }
    }

    // ------------------------------------------------------------
    // Coordinates validation
    // ------------------------------------------------------------
    private void validateCoordinates(AnnouncementDTO dto, List<String> errors) {

        // Posting coordinates
        if (dto.postingLatitude < -90 || dto.postingLatitude > 90) {
            errors.add("error.posting.latitude.range");
        }
        if (dto.postingLongitude < -180 || dto.postingLongitude > 180) {
            errors.add("error.posting.longitude.range");
        }

        // Reception coordinates
        if (dto.receptionLatitude < -90 || dto.receptionLatitude > 90) {
            errors.add("error.reception.latitude.range");
        }
        if (dto.receptionLongitude < -180 || dto.receptionLongitude > 180) {
            errors.add("error.reception.longitude.range");
        }

        // Optional: prevent identical coordinates
        if (dto.postingLatitude == dto.receptionLatitude &&
                dto.postingLongitude == dto.receptionLongitude) {
            errors.add("error.coordinates.same");
        }
    }

    // ------------------------------------------------------------
    // Dates validation
    // ------------------------------------------------------------
    private void validateDates(AnnouncementDTO dto, List<String> errors) {

        if (dto.postingDateTime == null || dto.receptionDateTime == null) return;

        if (dto.postingDateTime.isAfter(dto.receptionDateTime)) {
            errors.add("error.date.order");
        }
    }

    // ------------------------------------------------------------
    // Dimensions validation (optional)
    // ------------------------------------------------------------
    private void validateDimensions(AnnouncementDTO dto, List<String> errors) {

        if (dto.dimensions == null) return;

        var d = dto.dimensions;

        if (d.width <= 0) {
            errors.add("error.dimensions.width.positive");
        }
        if (d.height <= 0) {
            errors.add("error.dimensions.height.positive");
        }
        if (d.length <= 0) {
            errors.add("error.dimensions.length.positive");
        }

        if (d.unit == null || d.unit.isBlank()) {
            errors.add("error.dimensions.unit.required");
        } else if (!d.unit.equals("metric") && !d.unit.equals("imperial")) {
            errors.add("error.dimensions.unit.invalid");
        }
    }

    // ------------------------------------------------------------
    // Weight validation (optional)
    // ------------------------------------------------------------
    private void validateWeight(AnnouncementDTO dto, List<String> errors) {

        if (dto.weight == null) return;

        var w = dto.weight;

        if (w.value <= 0) {
            errors.add("error.weight.positive");
        }

        if (w.unit == null || w.unit.isBlank()) {
            errors.add("error.weight.unit.required");
        } else if (!w.unit.equals("metric") && !w.unit.equals("imperial")) {
            errors.add("error.weight.unit.invalid");
        }
    }


    // ------------------------------------------------------------
    // Translations validation (required)
    // ------------------------------------------------------------
    private void validateTranslations(AnnouncementDTO dto, List<String> errors) {

        if (dto.translations == null || dto.translations.isEmpty()) {
            errors.add("error.translation.required");
            return;
        }

        if (dto.translations.size() != 1) {
            errors.add("error.translation.single");
            return;
        }

        var t = dto.translations.get(0);

        if (t.language == null || t.language.isBlank()) {
            errors.add("error.translation.language.required");
        }

        if (!t.language.equals("en") && !t.language.equals("pl")) {
            errors.add("error.translation.language.invalid");
        }

        if (t.title == null || t.title.isBlank()) {
            errors.add("error.translation.title.required");
        }
    }

    private void validateStops(AnnouncementDTO dto, List<String> errors) {

        if (dto.stops == null) return; // stops are optional

        for (int i = 0; i < dto.stops.size(); i++) {
            StopDTO s = dto.stops.get(i);

            if (s.address == null || s.address.isBlank()) {
                errors.add("error.stop.address.required:" + (i + 1));
            }

            if (s.latitude == null) {
                errors.add("error.stop.latitude.required:" + (i + 1));
            } else if (s.latitude < -90 || s.latitude > 90) {
                errors.add("error.stop.latitude.range:" + (i + 1));
            }

            if (s.longitude == null) {
                errors.add("error.stop.longitude.required:" + (i + 1));
            } else if (s.longitude < -180 || s.longitude > 180) {
                errors.add("error.stop.longitude.range:" + (i + 1));
            }
        }
    }
}
