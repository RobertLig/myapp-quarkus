package org.example.myapp.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Locale;
import org.example.myapp.i18n.MessageService;

@ApplicationScoped
public class EmailService {

    @Inject
    MessageService messageService;

    @ConfigProperty(name = "resend.api.key")
    String apiKey;

    @ConfigProperty(name = "resend.from")
    String from;

    @ConfigProperty(name = "resend.base.url")
    String baseUrl;

    public void sendActionEmail(String email, String localeCode, String subjectKey, String introKey, String buttonKey, String fallbackKey, String link) {

        Locale locale = localeCode.equals("pl") ? Locale.of("pl", "PL") : Locale.ENGLISH;

        String subject = messageService.get(subjectKey, locale);
        String intro = messageService.get(introKey, locale);
        String button = messageService.get(buttonKey, locale);
        String fallback = messageService.get(fallbackKey, locale);

        String html = """
<div style="font-family: Arial, sans-serif; padding: 20px;
            background-color: oklch(98%% 0.016 73.684);
            color: oklch(40%% 0.123 38.172);">

  <h2 style="color: oklch(40%% 0.123 38.172);">%s</h2>

  <p style="font-size: 16px;">
    %s
  </p>

  <a href="%s"
     style="display: inline-block;
            padding: 10px 20px;
            background-color: oklch(46.44%% 0.111 37.85);
            color: oklch(90%% 0.076 70.697);
            text-decoration: none;
            border-radius: 6px;
            margin-top: 10px;">
    %s
  </a>

  <p style="font-size: 14px;
            color: oklch(40%% 0.123 38.172);
            margin-top: 20px;">
    %s<br>%s
  </p>

</div>
""".formatted(subject, intro, link, button, fallback, link);

        String body = """
    {
      "from": "%s",
      "to": "%s",
      "subject": "%s",
      "html": "%s"
    }
    """.formatted(from, email, subject, html);

        // send via Resend API...
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/emails"))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

        } catch (Exception e) {
            throw new RuntimeException("Failed to send verification email", e);
        }
    }
}
