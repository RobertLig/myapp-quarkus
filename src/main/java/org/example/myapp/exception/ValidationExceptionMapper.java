package org.example.myapp.exception;

import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import org.example.myapp.i18n.MessageService;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Provider
public class ValidationExceptionMapper implements ExceptionMapper<ValidationException> {

    @Inject
    MessageService messageService;

    @Inject
    HttpHeaders headers;

    @Override
    public Response toResponse(ValidationException exception) {

        if (exception instanceof ConstraintViolationException cve) {

            Set<ConstraintViolation<?>> violations =
                    cve.getConstraintViolations();

            Map<String, String> errors = new HashMap<>();

            for (ConstraintViolation<?> violation : violations) {

                String field = violation.getPropertyPath().toString();

                String template = violation.getMessageTemplate();

                String key = template;

                if (key.startsWith("{") && key.endsWith("}")) {
                    key = key.substring(1, key.length() - 1);
                }

                String message = messageService.get(key, headers);

                errors.put(field, message);
            }

            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of(
                            "error", messageService.get("validation.error", headers),
                            "details", errors
                    ))
                    .build();
        }

        return Response.status(Response.Status.BAD_REQUEST)
                .entity(Map.of(
                        "error", messageService.get("validation.error", headers)
                ))
                .build();
    }
}