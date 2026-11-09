package org.example.myapp.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.TransactionPhase;
import jakarta.inject.Inject;

import io.quarkus.mailer.Mail;
import io.quarkus.mailer.reactive.ReactiveMailer;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import java.util.Locale;

import org.example.myapp.i18n.MessageService;

@ApplicationScoped
public class EmailService {

    @Inject
    ReactiveMailer mailer;

    @Inject
    Event<Mail> emailRequests;

    @Inject
    MessageService messageService;

    private static final Logger log = Logger.getLogger(EmailService.class);

    @ConfigProperty(name = "quarkus.mailer.from")
    String from;

    public void sendActionEmail(String email, String localeCode,
                                String subjectKey, String introKey,
                                String buttonKey, String fallbackKey,
                                String link) {

        Locale locale = localeCode.equals("pl") ? Locale.of("pl", "PL") : Locale.ENGLISH;

        String subject = messageService.get(subjectKey, locale);
        String intro = messageService.get(introKey, locale);
        String button = messageService.get(buttonKey, locale);
        String fallback = messageService.get(fallbackKey, locale);

        String html = """
<div style="font-family: Arial, sans-serif; padding: 20px;
            background-color: oklch(98%% 0.016 73.684);
            color: oklch(40%% 0.123 38.172);">

  <h2>%s</h2>
  <p>%s</p>

  <a href="%s"
     style="display:inline-block;padding:10px 20px;background-color:oklch(46.44%% 0.111 37.85);
            color:oklch(90%% 0.076 70.697);text-decoration:none;border-radius:6px;">
    %s
  </a>

  <p style="margin-top:20px;">%s<br>%s</p>
</div>
""".formatted(subject, intro, link, button, fallback, link);

        emailRequests.fire(Mail.withHtml(email, subject, html).setFrom(from));
    }

    void sendAfterCommit(@Observes(during = TransactionPhase.AFTER_SUCCESS) Mail mail) {
        mailer.send(mail).subscribe().with(
                ignored -> log.info("Action email sent"),
                failure -> log.error("Failed to send action email", failure)
        );
    }
}
