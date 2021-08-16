package org.nakvaksin.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.vertx.ConsumeEvent;
import io.smallrye.common.annotation.Blocking;
import io.vertx.core.eventbus.EventBus;
import org.apache.commons.lang3.StringUtils;
import org.nakvaksin.domain.*;
import org.nakvaksin.repository.VaccinationStatusRepository;
import org.nakvaksin.web.rest.vm.DataElement;
import org.nakvaksin.web.rest.vm.StageElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.ExecutionException;

@ApplicationScoped
public class VaccinationStatusService {
    private final Logger log = LoggerFactory.getLogger(VaccinationStatusService.class);
    private final String TEXT_FIRST_DOSE_APPOINTMENT = "1st Dose appointment";
    private final String TEXT_SECOND_DOSE_APPOINTMENT = "2nd Dose appointment";

    @Inject
    VaccinationStatusRepository vaccinationStatusRepository;

    @Inject
    SubscriptionService subscriptionService;

    @Inject
    UserService userService;

    @Inject
    NotificationService notificationService;

    @Inject
    NotificationMessageGenerator notificationMessageGenerator;

    @Inject
    EventBus eventBus;

    @ConsumeEvent("update-vaccination-status-channel")
    @Blocking
    public void createOrUpdateVaccinationStatus(VaccinationStatus vacStatus) {
        log.debug("createOrUpdateVaccinationStatus : {}", vacStatus);

        try {
            VaccinationStatus status = vaccinationStatusRepository.saveOrUpdateVaccinationStatus(vacStatus);
            if (status.getVacPreviousJson() != null) {
                eventBus.send("compare-vac-status-channel", status);
            }
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            log.error("Error when creating or updating vac status: {}", e.getMessage());
        }
    }

    @ConsumeEvent("compare-vac-status-channel")
    @Blocking
    public void compareVacStatus(VaccinationStatus status) {
        log.debug("compareVacStatus : {}", status);

        ObjectMapper mapper = new ObjectMapper();
        try {
            // If no previous vaccination status, nothing to compare, end processing
            if (StringUtils.isBlank(status.getVacPreviousJson()))
                return;

            Optional<Subscription> subOpt = subscriptionService.getByUserId(status.getUserId());
            if (!subOpt.isPresent())
                return;
            Subscription subscription = subOpt.get();

            User user = userService.getUserByUserId(status.getUserId());

            StageElement[] prevNodeStageElements = mapper.readValue(status.getVacPreviousJson(), StageElement[].class);
            StageElement[] currNodeStageElements = mapper.readValue(status.getVacCurrentJson(), StageElement[].class);

            // Check 1st dose appointment for new schedule or updates
            StageElement prevFirstDoseEl = findStage(prevNodeStageElements, TEXT_FIRST_DOSE_APPOINTMENT);
            StageElement currFirstDoseEl = findStage(currNodeStageElements, TEXT_FIRST_DOSE_APPOINTMENT);

            if (prevFirstDoseEl != null && currFirstDoseEl != null) {
                if (!prevFirstDoseEl.getState().equalsIgnoreCase(currFirstDoseEl.getState()) && "ACTIVE".equalsIgnoreCase(currFirstDoseEl.getState())) {
                    log.debug("1st dose appointment SCHEDULED");
                    Notification ntf = notificationMessageGenerator.constructFirstDoseAppointmentMessage(user.getDisplayName(), subscription, currFirstDoseEl, status.getVacPreviousJson(), status.getVacCurrentJson());
                    notificationService.saveNotification(ntf);
                } else if ("ACTIVE".equalsIgnoreCase(prevFirstDoseEl.getState())
                    && "ACTIVE".equalsIgnoreCase(currFirstDoseEl.getState())
                    && isAppointmentChanged(prevFirstDoseEl.getData(), currFirstDoseEl.getData())) {

                    log.debug("1st dose appointment CHANGED");
                    Notification ntf = notificationMessageGenerator.constructFirstDoseAppointmentChangedMessage(user.getDisplayName(), subscription, currFirstDoseEl, status.getVacPreviousJson(), status.getVacCurrentJson());
                    notificationService.saveNotification(ntf);
                }
            }

            // Check 2nd dose appointment for new schedule or updates
            StageElement prevSecondDoseEl = findStage(prevNodeStageElements, TEXT_SECOND_DOSE_APPOINTMENT);
            StageElement currSecondDoseEl = findStage(currNodeStageElements, TEXT_SECOND_DOSE_APPOINTMENT);

            if (prevSecondDoseEl != null && currSecondDoseEl != null) {
                if (!prevSecondDoseEl.getState().equalsIgnoreCase(currSecondDoseEl.getState()) && "ACTIVE".equalsIgnoreCase(currSecondDoseEl.getState())) {
                    log.debug("2nd dose appointment SCHEDULED");
                    Notification ntf = notificationMessageGenerator.constructSecondDoseAppointmentScheduledMessage(user.getDisplayName(), subscription, currSecondDoseEl, status.getVacPreviousJson(), status.getVacCurrentJson());
                    notificationService.saveNotification(ntf);
                } else if ("ACTIVE".equalsIgnoreCase(prevSecondDoseEl.getState())
                    && "ACTIVE".equalsIgnoreCase(currSecondDoseEl.getState())
                    && isAppointmentChanged(prevSecondDoseEl.getData(), currSecondDoseEl.getData())) {

                    log.debug("2nd dose appointment CHANGED");
                    Notification ntf = notificationMessageGenerator.constructSecondDoseAppointmentChangedMessage(user.getDisplayName(), subscription, currSecondDoseEl, status.getVacPreviousJson(), status.getVacCurrentJson());
                    notificationService.saveNotification(ntf);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Error when comparing vac status: {}", e.getMessage());
        }
    }

    private StageElement findStage(StageElement[] stages, String searchText) {
        for (StageElement stage : stages) {
            if (searchText.equalsIgnoreCase(stage.getHeaderText().getEn_US())) {
                return stage;
            }
        }

        return null;
    }

    private DataElement findElement(List<DataElement> elements, String searchText) {
        for (DataElement el : elements) {
            if (searchText.equalsIgnoreCase(el.getText().getEn_US())) {
                return el;
            }
        }

        return null;
    }

    private boolean isAppointmentChanged(List<DataElement> prevElements, List<DataElement> currElements) {
        for (DataElement prevEl : prevElements) {
            DataElement currEl = findElement(currElements, prevEl.getText().getEn_US());
            if (currEl == null)
                return true;
            if (!prevEl.getValue().equalsIgnoreCase(currEl.getValue())) {
                return true;
            }
        }

        return false;
    }
}
