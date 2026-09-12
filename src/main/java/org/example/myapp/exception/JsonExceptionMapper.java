package org.example.myapp.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import org.example.myapp.i18n.MessageService;

import java.util.Map;

@Provider
public class JsonExceptionMapper implements ExceptionMapper<InvalidFormatException> {

    @Inject
    MessageService messageService;

    @Inject
    HttpHeaders headers;

    @Override
    public Response toResponse(InvalidFormatException exception) {

        String field = extractFieldName(exception);

        String messageKey = "validation." + field + ".invalid";

        String message;
        try {
            message = messageService.get(messageKey, headers);
        } catch (Exception e) {
            message = messageService.get("validation.value.invalid", headers);
        }

        return Response.status(Response.Status.BAD_REQUEST)
                .entity(Map.of(
                        "error", messageService.get("validation.error", headers),
                        "details", Map.of(field, message)
                ))
                .build();
    }

    private String extractFieldName(InvalidFormatException exception) {

        if (exception.getPath() != null && !exception.getPath().isEmpty()) {

            var reference = exception.getPath().get(exception.getPath().size() - 1);

            if (reference.getFieldName() != null) {
                return reference.getFieldName();
            }
        }

        return "value";
    }
}
