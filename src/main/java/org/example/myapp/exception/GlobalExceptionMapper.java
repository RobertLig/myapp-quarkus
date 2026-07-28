package org.example.myapp.exception;

import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import org.example.myapp.i18n.MessageService;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Exception> {

    @Inject
    MessageService messageService;

    @Inject
    HttpHeaders headers;

    @Override
    public Response toResponse(Exception exception) {

        // Validation errors
        if (exception instanceof ConstraintViolationException cve) {
            return handleValidationException(cve);
        }

        // Not found
        if (exception instanceof NotFoundException nfe) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of(
                            "error", messageService.get("error.notfound", headers),
                            "message", nfe.getMessage()
                    ))
                    .build();
        }

        // Illegal arguments
        if (exception instanceof IllegalArgumentException iae) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of(
                            "error", messageService.get("error.badrequest", headers),
                            "message", iae.getMessage()
                    ))
                    .build();
        }

        // Custom validator errors (WebApplicationException thrown manually)
        if (exception instanceof jakarta.ws.rs.WebApplicationException wae) {

            String raw = wae.getMessage();

            String key = raw;
            String param = null;

            if (raw.contains(":")) {
                String[] parts = raw.split(":", 2);
                key = parts[0];
                param = parts[1];
            }

            String translated = messageService.get(key, headers);

            if (param != null) {
                translated = translated.replace("{0}", param);
            }

            return Response.status(wae.getResponse().getStatus())
                    .entity(Map.of(
                            "error", translated
                    ))
                    .build();
        }

        // Internal server error
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Map.of(
                        "error", messageService.get("error.internal", headers),
                        "message", exception.getMessage()
                ))
                .build();
    }

    private Response handleValidationException(ConstraintViolationException cve) {
        Set<ConstraintViolation<?>> violations = cve.getConstraintViolations();
        Map<String, String> errors = new HashMap<>();

        for (ConstraintViolation<?> v : violations) {
            String field = v.getPropertyPath().toString();
            String message = v.getMessage();
            errors.put(field, message);
        }

        return Response.status(Response.Status.BAD_REQUEST)
                .entity(Map.of(
                        "error", messageService.get("validation.error", headers),
                        "details", errors
                ))
                .build();
    }
}
