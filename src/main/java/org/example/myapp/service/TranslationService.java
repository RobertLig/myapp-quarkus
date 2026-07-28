package org.example.myapp.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import org.example.myapp.dto.TranslatedText;

@ApplicationScoped
public class TranslationService {

    private static final String API_URL = "https://libretranslate.com/translate";

    private final Client client = ClientBuilder.newClient();

    public TranslatedText translate(String text, String targetLang) {

        try {
            JsonObject requestJson = Json.createObjectBuilder()
                    .add("q", text)
                    .add("source", "auto")
                    .add("target", targetLang)
                    .add("format", "text")
                    .build();

            JsonObject responseJson = client
                    .target(API_URL)
                    .request(MediaType.APPLICATION_JSON)
                    .post(Entity.json(requestJson), JsonObject.class);

            String translated = responseJson.getString("translatedText");

            return new TranslatedText(translated);

        } catch (Exception e) {
            // You can log this if needed
            throw new RuntimeException("Translation failed: " + e.getMessage());
        }
    }
}
