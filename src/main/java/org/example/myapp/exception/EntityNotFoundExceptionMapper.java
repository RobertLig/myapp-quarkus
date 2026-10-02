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
public class EntityNotFoundExceptionMapper implements ExceptionMapper<EntityNotFoundException> {

    @Inject
    MessageService messageService;

    @Inject
    HttpHeaders headers;

    @Override
    public Response toResponse(EntityNotFoundException exception) {
        String translatedMessage;
        try {
            translatedMessage = messageService.get(exception.getMessageKey(), headers);
        } catch (MissingResourceException e) {
            translatedMessage = exception.getMessageKey();
        }

        return Response.status(Response.Status.NOT_FOUND)
                .entity(Map.of("error", translatedMessage))
                .build();
    }
}