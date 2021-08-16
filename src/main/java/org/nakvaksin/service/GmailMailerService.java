package org.nakvaksin.service;

import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.nakvaksin.domain.Email;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class GmailMailerService {
    private final Logger log = LoggerFactory.getLogger(GmailMailerService.class);

    @Inject
    Mailer mailer;

    @ConfigProperty(name = "quarkus.mailer.from")
    String fromEmail;

    public void sendEmail(Email email) {
        Mail mail = Mail.withHtml(email.getTo(), email.getSubject(), email.getBody());
        mail.setFrom(fromEmail);
        mailer.send(mail);
    }
}
