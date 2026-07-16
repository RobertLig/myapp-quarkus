package org.example.myapp.i18n;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.HttpHeaders;

import java.util.Locale;
import java.util.ResourceBundle;

@ApplicationScoped
public class MessageService {

    public String get(String key, HttpHeaders headers) {
        Locale locale = resolveLocale(headers);
        ResourceBundle bundle = ResourceBundle.getBundle("i18n/messages", locale);
        return bundle.getString(key);
    }

    private Locale resolveLocale(HttpHeaders headers) {
        String lang = headers.getHeaderString("Accept-Language");
        if (lang == null) return Locale.ENGLISH;

        if (lang.startsWith("pl")) return Locale.of("pl", "PL");
        return Locale.ENGLISH;
    }
}
