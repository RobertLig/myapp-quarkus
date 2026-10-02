package org.example.myapp.exception;

import jakarta.inject.Inject;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.example.myapp.i18n.MessageService;

import java.util.Map;
import java.util.MissingResourceException;

@Provider
public class FieldValidationExceptionMapper implements ExceptionMapper<FieldValidationException> {

    @Inject
    MessageService messageService;

    @Inject
    HttpHeaders headers;

    @Override
    public Response toResponse(FieldValidationException exception) {
        String translatedMessage;
        try {
            translatedMessage = messageService.get(exception.getMessageKey(), headers);
        } catch (MissingResourceException e) {
            translatedMessage = exception.getMessageKey();
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
                        "details", Map.of(exception.getField(), translatedMessage)
                ))
                .build();
    }
}