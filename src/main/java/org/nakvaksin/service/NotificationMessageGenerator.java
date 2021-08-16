package org.nakvaksin.service;

import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import org.apache.commons.lang3.StringUtils;
import org.nakvaksin.domain.Notification;
import org.nakvaksin.domain.NotificationChannelStatus;
import org.nakvaksin.domain.Subscription;
import org.nakvaksin.domain.UnSub;
import org.nakvaksin.web.rest.vm.DataElement;
import org.nakvaksin.web.rest.vm.StageElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@ApplicationScoped
public class NotificationMessageGenerator {
    private final Logger log = LoggerFactory.getLogger(NotificationMessageGenerator.class);

    private final String TEXT_DOSE1_APPOINTMENT_ASSESSMENT = "DOSE1_APPOINTMENT_ASSESSMENT";
    private final String TEXT_DOSE2_APPOINTMENT_ASSESSMENT = "DOSE2_APPOINTMENT_ASSESSMENT";

    @Location("appointment_1st_dose_self_v1")
    Template firstDoseAppointmentSelfTemplate;

    @Location("appointment_1st_dose_family_v1")
    Template firstDoseAppointmentFamilyTemplate;

    @Location("appointment_updated_1st_dose_self_v1")
    Template firstDoseAppointmentUpdatedSelfTemplate;

    @Location("appointment_updated_1st_dose_family_v1")
    Template firstDoseAppointmentUpdatedFamilyTemplate;

    @Location("appointment_2nd_dose_self_v1")
    Template secondDoseAppointmentSelfTemplate;

    @Location("appointment_2nd_dose_family_v1")
    Template secondDoseAppointmentFamilyTemplate;

    @Location("appointment_updated_2nd_dose_self_v1")
    Template secondDoseAppointmentUpdatedSelfTemplate;

    @Location("appointment_updated_2nd_dose_family_v1")
    Template secondDoseAppointmentUpdatedFamilyTemplate;

    @Inject
    UnsubService unsubService;

    public Notification constructFirstDoseAppointmentMessage(String name, Subscription subscription, StageElement element, String prevJson, String currJson) {
        log.debug("constructFirstDoseAppointmentMessage...");

        Notification ntf = new Notification();
        List<NotificationChannelStatus> list = new ArrayList<>();
        if (StringUtils.isNotBlank(subscription.getUserPhoneNumber())) {
            String unsubKey = getUnsubKey(subscription.getUserPhoneNumber());
            String userSmsText = "NakVaksin: Your 1st dose vaccination appointment has been scheduled. Please check your MySejahtera app.\n\nClick to unsubscribe: https://nakvaksin.com/r/" + unsubKey;
            list.add(new NotificationChannelStatus(NotificationChannelStatus.CHANNEL_TYPE_SMS, userSmsText, subscription.getUserPhoneNumber(), null));
        }
        if (StringUtils.isNotBlank(subscription.getFamilyPhoneNumber())) {
            String unsubKey = getUnsubKey(subscription.getFamilyPhoneNumber());
            String familySmsText = "NakVaksin: 1st dose vaccination appointment for " + sanitizeName(name) + " has been scheduled. Please remind him/her to check MySejahtera app.\n\nClick to unsubscribe: https://nakvaksin.com/r/" + unsubKey;
            list.add(new NotificationChannelStatus(NotificationChannelStatus.CHANNEL_TYPE_SMS, familySmsText, subscription.getFamilyPhoneNumber(), null));
        }
        if (StringUtils.isNotBlank(subscription.getUserEmail())) {
            String unsubKey = getUnsubKey(subscription.getUserEmail());

            boolean confirm = false;
            if (containsValue(element.getAction(), TEXT_DOSE1_APPOINTMENT_ASSESSMENT)) {
                confirm = true;
            }

            String userEmailHTML = firstDoseAppointmentSelfTemplate
                .data("elements", element.getData())
                .data("confirm", confirm)
                .data("unsubKey", unsubKey)
                .render();
            log.debug("userEmailHTML : {}", userEmailHTML);

            list.add(new NotificationChannelStatus(NotificationChannelStatus.CHANNEL_TYPE_EMAIL, userEmailHTML, subscription.getUserEmail(), null));
        }
        if (StringUtils.isNotBlank(subscription.getFamilyEmail())) {
            String unsubKey = getUnsubKey(subscription.getFamilyEmail());

            String familyEmailHTML = firstDoseAppointmentFamilyTemplate
                .data("elements", element.getData())
                .data("name", name)
                .data("unsubKey", unsubKey)
                .render();
            log.debug("familyEmailHTML : {}", familyEmailHTML);

            list.add(new NotificationChannelStatus(NotificationChannelStatus.CHANNEL_TYPE_EMAIL, familyEmailHTML, subscription.getFamilyEmail(), null));
        }
        ntf.setChannels(list);
        ntf.setUserId(subscription.getUserId());
        ntf.setAllSent(false);
        ntf.setCurrVacStatusJson(currJson);
        ntf.setPrevVacStatusJson(prevJson);
        ntf.setCreatedDate(new Date());

        return ntf;
    }

    public Notification constructFirstDoseAppointmentChangedMessage(String name, Subscription subscription, StageElement element, String prevJson, String currJson) {
        log.debug("constructFirstDoseAppointmentChangedMessage...");

        Notification ntf = new Notification();
        List<NotificationChannelStatus> list = new ArrayList<>();
        if (StringUtils.isNotBlank(subscription.getUserPhoneNumber())) {
            String userSmsText = "NakVaksin: Your 1st dose vaccination appointment has changed. Please check your MySejahtera app.";
            list.add(new NotificationChannelStatus(NotificationChannelStatus.CHANNEL_TYPE_SMS, userSmsText, subscription.getUserPhoneNumber(), null));
        }
        if (StringUtils.isNotBlank(subscription.getFamilyPhoneNumber())) {
            String familySmsText = "NakVaksin: 1st dose vaccination appointment for " + sanitizeName(name) + " has changed. Please remind him/her to check MySejahtera app.";
            list.add(new NotificationChannelStatus(NotificationChannelStatus.CHANNEL_TYPE_SMS, familySmsText, subscription.getFamilyPhoneNumber(), null));
        }
        if (StringUtils.isNotBlank(subscription.getUserEmail())) {
            String unsubKey = getUnsubKey(subscription.getUserEmail());

            boolean confirm = false;
            if (containsValue(element.getAction(), TEXT_DOSE1_APPOINTMENT_ASSESSMENT)) {
                confirm = true;
            }

            String userEmailHTML = firstDoseAppointmentUpdatedSelfTemplate
                .data("elements", element.getData())
                .data("confirm", confirm)
                .data("unsubKey", unsubKey)
                .render();
            log.debug("userEmailHTML : {}", userEmailHTML);

            list.add(new NotificationChannelStatus(NotificationChannelStatus.CHANNEL_TYPE_EMAIL, userEmailHTML, subscription.getUserEmail(), null));
        }
        if (StringUtils.isNotBlank(subscription.getFamilyEmail())) {
            String unsubKey = getUnsubKey(subscription.getFamilyEmail());

            String familyEmailHTML = firstDoseAppointmentUpdatedFamilyTemplate
                .data("elements", element.getData())
                .data("name", name)
                .data("unsubKey", unsubKey)
                .render();
            log.debug("familyEmailHTML : {}", familyEmailHTML);

            list.add(new NotificationChannelStatus(NotificationChannelStatus.CHANNEL_TYPE_EMAIL, familyEmailHTML, subscription.getFamilyEmail(), null));
        }
        ntf.setChannels(list);
        ntf.setUserId(subscription.getUserId());
        ntf.setAllSent(false);
        ntf.setCurrVacStatusJson(currJson);
        ntf.setPrevVacStatusJson(prevJson);
        ntf.setCreatedDate(new Date());

        return ntf;
    }

    public Notification constructSecondDoseAppointmentScheduledMessage(String name, Subscription subscription, StageElement element, String prevJson, String currJson) {
        log.debug("constructSecondDoseAppointmentScheduledMessage...");

        Notification ntf = new Notification();
        List<NotificationChannelStatus> list = new ArrayList<>();
        if (StringUtils.isNotBlank(subscription.getUserPhoneNumber())) {
            String userSmsText = "NakVaksin: Your 2nd dose vaccination appointment has been scheduled. Please check your MySejahtera app.";
            list.add(new NotificationChannelStatus(NotificationChannelStatus.CHANNEL_TYPE_SMS, userSmsText, subscription.getUserPhoneNumber(), null));
        }
        if (StringUtils.isNotBlank(subscription.getFamilyPhoneNumber())) {
            String familySmsText = "NakVaksin: 2nd dose vaccination appointment for " + sanitizeName(name) + " has been scheduled. Please remind him/her to check MySejahtera app.";
            list.add(new NotificationChannelStatus(NotificationChannelStatus.CHANNEL_TYPE_SMS, familySmsText, subscription.getFamilyPhoneNumber(), null));
        }
        if (StringUtils.isNotBlank(subscription.getUserEmail())) {
            String unsubKey = getUnsubKey(subscription.getUserEmail());

            boolean confirm = false;
            if (containsValue(element.getAction(), TEXT_DOSE2_APPOINTMENT_ASSESSMENT)) {
                confirm = true;
            }

            String userEmailHTML = secondDoseAppointmentSelfTemplate
                .data("elements", element.getData())
                .data("confirm", confirm)
                .data("unsubKey", unsubKey)
                .render();
            log.debug("userEmailHTML : {}", userEmailHTML);

            list.add(new NotificationChannelStatus(NotificationChannelStatus.CHANNEL_TYPE_EMAIL, userEmailHTML, subscription.getUserEmail(), null));
        }
        if (StringUtils.isNotBlank(subscription.getFamilyEmail())) {
            String unsubKey = getUnsubKey(subscription.getFamilyEmail());

            String familyEmailHTML = secondDoseAppointmentFamilyTemplate
                .data("elements", element.getData())
                .data("name", name)
                .data("unsubKey", unsubKey)
                .render();
            log.debug("familyEmailHTML : {}", familyEmailHTML);

            list.add(new NotificationChannelStatus(NotificationChannelStatus.CHANNEL_TYPE_EMAIL, familyEmailHTML, subscription.getFamilyEmail(), null));
        }
        ntf.setChannels(list);
        ntf.setUserId(subscription.getUserId());
        ntf.setAllSent(false);
        ntf.setCurrVacStatusJson(currJson);
        ntf.setPrevVacStatusJson(prevJson);
        ntf.setCreatedDate(new Date());

        return ntf;
    }

    public Notification constructSecondDoseAppointmentChangedMessage(String name, Subscription subscription, StageElement element, String prevJson, String currJson) {
        log.debug("constructSecondDoseAppointmentChangedMessage...");

        Notification ntf = new Notification();
        List<NotificationChannelStatus> list = new ArrayList<>();
        if (StringUtils.isNotBlank(subscription.getUserPhoneNumber())) {
            String userSmsText = "NakVaksin: Your 2nd dose vaccination appointment has changed. Please check your MySejahtera app.";
            list.add(new NotificationChannelStatus(NotificationChannelStatus.CHANNEL_TYPE_SMS, userSmsText, subscription.getUserPhoneNumber(), null));
        }
        if (StringUtils.isNotBlank(subscription.getFamilyPhoneNumber())) {
            String familySmsText = "NakVaksin: 2nd dose vaccination appointment for " + sanitizeName(name) + " has changed. Please remind him/her to check MySejahtera app.";
            list.add(new NotificationChannelStatus(NotificationChannelStatus.CHANNEL_TYPE_SMS, familySmsText, subscription.getFamilyPhoneNumber(), null));
        }
        if (StringUtils.isNotBlank(subscription.getUserEmail())) {
            String unsubKey = getUnsubKey(subscription.getUserEmail());

            boolean confirm = false;
            if (containsValue(element.getAction(), TEXT_DOSE2_APPOINTMENT_ASSESSMENT)) {
                confirm = true;
            }

            String userEmailHTML = secondDoseAppointmentUpdatedSelfTemplate
                .data("elements", element.getData())
                .data("confirm", confirm)
                .data("unsubKey", unsubKey)
                .render();
            log.debug("userEmailHTML : {}", userEmailHTML);

            list.add(new NotificationChannelStatus(NotificationChannelStatus.CHANNEL_TYPE_EMAIL, userEmailHTML, subscription.getUserEmail(), null));
        }
        if (StringUtils.isNotBlank(subscription.getFamilyEmail())) {
            String unsubKey = getUnsubKey(subscription.getFamilyEmail());

            String familyEmailHTML = secondDoseAppointmentUpdatedFamilyTemplate
                .data("elements", element.getData())
                .data("name", name)
                .data("unsubKey", unsubKey)
                .render();
            log.debug("familyEmailHTML : {}", familyEmailHTML);

            list.add(new NotificationChannelStatus(NotificationChannelStatus.CHANNEL_TYPE_EMAIL, familyEmailHTML, subscription.getFamilyEmail(), null));
        }
        ntf.setChannels(list);
        ntf.setUserId(subscription.getUserId());
        ntf.setAllSent(false);
        ntf.setCurrVacStatusJson(currJson);
        ntf.setPrevVacStatusJson(prevJson);
        ntf.setCreatedDate(new Date());

        return ntf;
    }

    private boolean containsValue(List<DataElement> elements, String searchText) {
        for (DataElement el : elements) {
            if (searchText.equalsIgnoreCase(el.getValue())) {
                return true;
            }
        }
        return false;
    }

    private String sanitizeName(String name) {
        if (name.length() < 40)
            return name;

        return name.substring(0, 40);
    }

    private String getUnsubKey(String contact) {
        String unsubKey = "";
        if (StringUtils.isNotBlank(contact)) {
            UnSub unsub = unsubService.getUnsubForContact(contact);
            if (unsub != null) {
                unsubKey = unsub.getKey();
            } else {
                UnSub newUnsub = unsubService.createNewUnsubForContact(contact);
                unsubKey = newUnsub.getKey();
            }
        }
        return unsubKey;
    }
}
