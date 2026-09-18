package org.example.myapp.auth;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.WebApplicationException;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.Collections;

@ApplicationScoped
public class GoogleService {

    @ConfigProperty(name = "google.client.id")
    String googleClientId;

    private final GoogleIdTokenVerifier verifier =
            new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new JacksonFactory())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

    public GooglePayload verify(String idTokenString) {

        try {
            GoogleIdToken idToken = verifier.verify(idTokenString);

            if (idToken == null) {
                throw new WebApplicationException("error.google.invalid", 400);
            }

            GoogleIdToken.Payload payload = idToken.getPayload();

            GooglePayload result = new GooglePayload();
            result.setEmail(payload.getEmail());
            result.setName((String) payload.get("name"));
            result.setSubject(payload.getSubject());

            return result;

        } catch (Exception e) {
            throw new WebApplicationException("error.google.failed", 400);
        }
    }
}
