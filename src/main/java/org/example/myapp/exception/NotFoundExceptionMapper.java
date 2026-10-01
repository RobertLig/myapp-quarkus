package org.example.myapp.exception;

import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.example.myapp.i18n.MessageService;

import java.util.Map;
import java.util.MissingResourceException;

@Provider
public class NotFoundExceptionMapper implements ExceptionMapper<NotFoundException> {

    @Inject
    MessageService messageService;

    @Inject
    HttpHeaders headers;

    @Override
    public Response toResponse(NotFoundException exception) {
        String translatedTitle;
        try {
            translatedTitle = messageService.get("error.notfound", headers);
        } catch (MissingResourceException e) {
            translatedTitle = "Not found";
        }

        return Response.status(Response.Status.NOT_FOUND)
                .entity(Map.of(
                        "error", translatedTitle,
                        "message", exception.getMessage() != null ? exception.getMessage() : ""
                ))
                .build();
    }
}