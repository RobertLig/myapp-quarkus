package org.example.myapp.exception;

import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.example.myapp.i18n.MessageService;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.MissingResourceException;

@Provider
public class ValidationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

    @Inject
    MessageService messageService;

    @Inject
    HttpHeaders headers;

    @Override
    public Response toResponse(ConstraintViolationException exception) {
        Map<String, String> fieldDetails = new LinkedHashMap<>();

        for (ConstraintViolation<?> violation : exception.getConstraintViolations()) {
            String rawPath = violation.getPropertyPath().toString();
            String fieldKey = formatFieldPath(rawPath);

            String messageTemplate = violation.getMessage(); // e.g., "{error.dimensions.width.positive}"
            String translatedMessage;

            // Resolve message key if it's formatted as a property key enclosed in {}
            if (messageTemplate != null && messageTemplate.startsWith("{") && messageTemplate.endsWith("}")) {
                String key = messageTemplate.substring(1, messageTemplate.length() - 1);
                try {
                    translatedMessage = messageService.get(key, headers);
                } catch (MissingResourceException e) {
                    translatedMessage = key;
                }
            } else {
                translatedMessage = messageTemplate;
            }

            fieldDetails.put(fieldKey, translatedMessage);
        }

        String title;
        try {
            title = messageService.get("validation.error", headers);
        } catch (MissingResourceException e) {
            title = "Validation error";
        }

        return Response.status(Response.Status.BAD_REQUEST)
                .entity(Map.of(
                        "error", title,
                        "details", fieldDetails
                ))
                .build();
    }

    /**
     * Converts raw parameter paths like "createAnnouncement.dto.stops[0].address"
     * into clean field paths like "stops[0].address".
     */
    private String formatFieldPath(String rawPath) {
        if (rawPath == null || rawPath.isBlank()) {
            return "global";
        }

        // Split method/DTO path elements by '.'
        String[] parts = rawPath.split("\\.");

        // If path contains method name + param name (e.g. ["createAnnouncement", "dto", "stops[0]", "address"])
        // find where the actual payload object fields start.
        int startIndex = 0;
        for (int i = 0; i < parts.length; i++) {
            // Skip method name or common DTO/param names
            if (i < 2 && (parts[i].equals("dto") || parts[i].endsWith("DTO") || parts[i].contains("Announcement"))) {
                startIndex = i + 1;
            }
        }

        if (startIndex >= parts.length) {
            return parts[parts.length - 1];
        }

        return String.join(".", java.util.Arrays.copyOfRange(parts, startIndex, parts.length));
    }
}