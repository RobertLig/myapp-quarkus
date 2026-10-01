package org.example.myapp.exception;

import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.example.myapp.i18n.MessageService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;

@Provider
public class WebApplicationExceptionMapper implements ExceptionMapper<WebApplicationException> {

    @Inject
    MessageService messageService;

    @Inject
    HttpHeaders headers;

    @Override
    public Response toResponse(WebApplicationException wae) {
        String raw = wae.getMessage();
        if (raw == null || raw.isBlank()) {
            return wae.getResponse();
        }

        String[] parts = raw.split(";");
        List<String> translatedErrors = new ArrayList<>();

        for (String part : parts) {
            String key = part;
            String param = null;

            if (part.contains(":")) {
                String[] sub = part.split(":", 2);
                key = sub[0];
                param = sub[1];
            }

            String translated;
            try {
                translated = messageService.get(key, headers);
            } catch (MissingResourceException e) {
                translated = key;
            }

            if (param != null) {
                translated = translated.replace("{0}", param);
            }

            translatedErrors.add(translated);
        }

        return Response.status(wae.getResponse().getStatus())
                .entity(Map.of("errors", translatedErrors))
                .build();
    }
}