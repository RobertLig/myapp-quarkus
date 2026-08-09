package org.example.myapp.auth;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.WebApplicationException;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@ApplicationScoped
public class GoogleService {

    @ConfigProperty(name = "google.client.id")
    String googleClientId;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    public GooglePayload verify(String idToken) {

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://oauth2.googleapis.com/tokeninfo?id_token=" + idToken))
                    .GET()
                    .build();

            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new WebApplicationException("error.google.invalid", 400);
            }

            JsonNode json = mapper.readTree(response.body());

            // Validate audience (client ID)
            String aud = json.get("aud").asText();
            if (!aud.equals(googleClientId)) {
                throw new WebApplicationException("error.google.invalid", 400);
            }

            GooglePayload payload = new GooglePayload();
            payload.setEmail(json.get("email").asText());
            payload.setName(json.get("name").asText());
            payload.setSubject(json.get("sub").asText());

            return payload;

        } catch (Exception e) {
            throw new WebApplicationException("error.google.failed", 400);
        }
    }
}
