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
public class IllegalArgumentExceptionMapper implements ExceptionMapper<IllegalArgumentException> {

    @Inject
    MessageService messageService;

    @Inject
    HttpHeaders headers;

    @Override
    public Response toResponse(IllegalArgumentException exception) {
        String key = exception.getMessage();
        String message;

        try {
            message = messageService.get(key, headers);
        } catch (MissingResourceException e) {
            message = key; // Fallback if key is missing in .properties
        }

        String errorTitle;
        try {
            errorTitle = messageService.get("error.badrequest", headers);
        } catch (MissingResourceException e) {
            errorTitle = "Bad Request";
        }

        return Response.status(Response.Status.BAD_REQUEST)
                .entity(Map.of(
                        "error", errorTitle,
                        "message", message
                ))
                .build();
    }
}