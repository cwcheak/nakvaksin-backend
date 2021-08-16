package org.nakvaksin.service;

import io.quarkus.scheduler.Scheduled;
import io.quarkus.vertx.ConsumeEvent;
import io.smallrye.common.annotation.Blocking;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.nakvaksin.domain.AdaSMSResponse;
import org.nakvaksin.domain.Email;
import org.nakvaksin.domain.Notification;
import org.nakvaksin.domain.NotificationChannelStatus;
import org.nakvaksin.repository.NotificationRepository;
import org.nakvaksin.service.exception.DBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

@ApplicationScoped
public class NotificationService {
    private final Logger log = LoggerFactory.getLogger(NotificationService.class);

    @Inject
    NotificationRepository notificationRepository;

    @Inject
    GmailMailerService gmailMailerService;

    @Inject
    @RestClient
    AdaSMSService adaSMSService;

    @Inject
    UnsubService unsubService;

    @ConfigProperty(name = "adasms-api.token")
    String adaSMSToken;

    @ConfigProperty(name = "quarkus.mailer.from")
    String emailFrom;

    @ConfigProperty(name = "email.subject.appointment")
    String emailSubjectAppointment;

    @ConfigProperty(name = "email.subject.error")
    String emailSubjectError;

    @ConfigProperty(name = "email.to.error")
    String errorEmailAddress;

    public void saveNotification(Notification ntf) {
        log.debug("saveNotification...");
        log.debug(ntf.toString());

        try {
            notificationRepository.saveNotification(ntf);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            throw new DBException("Error saving notification : " + e.getMessage());
        }
    }

    // NOT THREAD SAFE. DO NOT SCHEDULE / RUN THIS CONCURRENTLY
    @Scheduled(cron = "{cron.expr.notificationSender}")
    void sendNotification() {
        log.debug("[Scheduler] sendNotification...");

        try {
            List<Notification> notificationList = notificationRepository.getAllUnsend();
            for (Notification notification : notificationList) {
                if (notification.isAllSent())
                    return;

                notification.getChannels().stream().forEach(ntfChannel -> {
                    if (ntfChannel.getSentDate() == null && !unsubService.isContactUnsub(ntfChannel.getContact())) {
                        switch (ntfChannel.getType()) {
                            case NotificationChannelStatus.CHANNEL_TYPE_SMS:
                                AdaSMSResponse response = adaSMSService.sendSMS(adaSMSToken, ntfChannel.getContact(), ntfChannel.getText());
                                log.debug("response : {}", response);

                                if (response.getSuccess() != null && response.getSuccess().booleanValue() == true) {
                                    ntfChannel.setSentDate(new Date());
                                } else {
                                    Email email = new Email();
                                    email.setFrom(emailFrom);
                                    email.setTo(errorEmailAddress);
                                    email.setSubject(emailSubjectError);
                                    email.setBody(response.toString());
                                    sendEmail(email);
                                }
                                break;

                            case NotificationChannelStatus.CHANNEL_TYPE_EMAIL:
                                Email email = new Email();
                                email.setFrom(emailFrom);
                                email.setTo(ntfChannel.getContact());
                                email.setSubject(emailSubjectAppointment);
                                email.setBody(ntfChannel.getText());
                                sendEmail(email);

                                ntfChannel.setSentDate(new Date());
                                break;

                            default:
                                break;
                        }
                    }
                });

                // Set all sent flag
                if (isAllSent(notification)) {
                    notification.setAllSent(true);
                }

                notificationRepository.updateNotification(notification);
            }
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            log.error("Error when sending notification : {}", e.getMessage());
        }
    }

    private boolean isAllSent(Notification ntf) {
        boolean allSent = true;
        for (NotificationChannelStatus channel : ntf.getChannels()) {
            if (channel.getSentDate() == null) {
                allSent = allSent && false;
            }
        }
        return allSent;
    }

    private void sendEmail(Email email) {
        gmailMailerService.sendEmail(email);
        log.debug("Send email successful!");
    }
}
