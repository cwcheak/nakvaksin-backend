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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@ApplicationScoped
public class VaccinationStatusService {
    private final Logger log = LoggerFactory.getLogger(VaccinationStatusService.class);
    private final String TEXT_FIRST_DOSE_APPOINTMENT = "1st Dose appointment";
    private final String TEXT_FIRST_DOSE_COMPLETED = "1st Dose completed";
    private final String TEXT_SECOND_DOSE_APPOINTMENT = "2nd Dose appointment";
    private final String TEXT_SECOND_DOSE_COMPLETED = "2nd Dose completed";
    private final String TEXT_APPOINTMENT_DATE = "Date:";

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
    WorkflowStageService workflowStageService;

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

            // Check if user subscribe to be notified
            Optional<Subscription> subOpt = subscriptionService.getByUserId(status.getUserId());
            if (!subOpt.isPresent())
                return;
            Subscription subscription = subOpt.get();

            StageElement[] prevNodeStageElements = mapper.readValue(status.getVacPreviousJson(), StageElement[].class);
            StageElement[] currNodeStageElements = mapper.readValue(status.getVacCurrentJson(), StageElement[].class);

            StageElement prevFirstDoseEl = findStage(prevNodeStageElements, TEXT_FIRST_DOSE_APPOINTMENT);
            StageElement currFirstDoseEl = findStage(currNodeStageElements, TEXT_FIRST_DOSE_APPOINTMENT);
            StageElement prevSecondDoseEl = findStage(prevNodeStageElements, TEXT_SECOND_DOSE_APPOINTMENT);
            StageElement currSecondDoseEl = findStage(currNodeStageElements, TEXT_SECOND_DOSE_APPOINTMENT);
            StageElement prevFirstDoseCompletedEl = findStage(prevNodeStageElements, TEXT_FIRST_DOSE_COMPLETED);
            StageElement currFirstDoseCompletedEl = findStage(currNodeStageElements, TEXT_FIRST_DOSE_COMPLETED);
            StageElement prevSecondDoseCompletedEl = findStage(prevNodeStageElements, TEXT_SECOND_DOSE_COMPLETED);
            StageElement currSecondDoseCompletedEl = findStage(currNodeStageElements, TEXT_SECOND_DOSE_COMPLETED);

            // Check user workflow stage
            WorkflowStage wfStage = workflowStageService.getByUserId(status.getUserId());
            if (wfStage == null) {
                wfStage = initializeWorkflowStage(status.getUserId(), currFirstDoseEl, currSecondDoseEl);
                workflowStageService.createOrUpdateWorkflowStage(wfStage);
            }

            User user = userService.getUserByUserId(status.getUserId());

            // If 1st dose has not complete
            if (wfStage.getCurrStage() < WorkflowStage.STAGE_FIRST_DOSE_COMPLETED && prevFirstDoseEl != null && currFirstDoseEl != null) {
                // Check 1st dose appointment for new schedule or updates
                if (!prevFirstDoseEl.getState().equalsIgnoreCase(currFirstDoseEl.getState()) && "ACTIVE".equalsIgnoreCase(currFirstDoseEl.getState())) {
                    log.debug("1st dose appointment SCHEDULED");

                    // Create and save notification
                    Notification ntf = notificationMessageGenerator.constructFirstDoseAppointmentMessage(user.getDisplayName(), subscription, currFirstDoseEl, status.getVacPreviousJson(), status.getVacCurrentJson());
                    notificationService.saveNotification(ntf);

                    // Update workflow stage
                    wfStage.setUserId(user.getUserId());
                    wfStage.setCurrStage(WorkflowStage.STAGE_FIRST_DOSE_APPOINTMENT_SCHEDULED);
                    wfStage.setStageHist(wfStage.getStageHist() + "->" + WorkflowStage.STAGE_FIRST_DOSE_APPOINTMENT_SCHEDULED);
                    workflowStageService.createOrUpdateWorkflowStage(wfStage);

                } else if ("ACTIVE".equalsIgnoreCase(prevFirstDoseEl.getState())
                    && "ACTIVE".equalsIgnoreCase(currFirstDoseEl.getState())
                    && isAppointmentChanged(prevFirstDoseEl.getData(), currFirstDoseEl.getData())) {
                    log.debug("1st dose appointment CHANGED");

                    // Create and save notification
                    Notification ntf = notificationMessageGenerator.constructFirstDoseAppointmentChangedMessage(user.getDisplayName(), subscription, currFirstDoseEl, status.getVacPreviousJson(), status.getVacCurrentJson());
                    notificationService.saveNotification(ntf);

                    // Update workflow stage
                    wfStage.setUserId(user.getUserId());
                    wfStage.setCurrStage(WorkflowStage.STAGE_FIRST_DOSE_APPOINTMENT_SCHEDULED);
                    wfStage.setStageHist(wfStage.getStageHist() + "->" + WorkflowStage.STAGE_FIRST_DOSE_APPOINTMENT_SCHEDULED);
                    workflowStageService.createOrUpdateWorkflowStage(wfStage);

                }

                // Check if 1st dose appointment is tomorrow
                DataElement el = findElement(currFirstDoseEl.getData(), TEXT_APPOINTMENT_DATE);
                String appointmentDate = null;
                if (el != null)
                    appointmentDate = el.getValue();

                if ("ACTIVE".equalsIgnoreCase(prevFirstDoseEl.getState())
                    && "ACTIVE".equalsIgnoreCase(currFirstDoseEl.getState())
                    && el != null
                    && isAppointmentTomorrow(appointmentDate)) {
                    log.debug("1st dose appointment is tomorrow, send reminder...");

                    // Create and save notification
                    Notification ntf = notificationMessageGenerator.constructFirstDoseAppointmentPriorDayReminderMessage(user.getDisplayName(), subscription, currFirstDoseEl, status.getVacPreviousJson(), status.getVacCurrentJson());
                    notificationService.saveNotification(ntf);

                    // Update workflow stage
                    wfStage.setUserId(user.getUserId());
                    wfStage.setCurrStage(WorkflowStage.STAGE_FIRST_DOSE_APPOINTMENT_PRIOR_DAY_REMINDER);
                    wfStage.setStageHist(wfStage.getStageHist() + "->" + WorkflowStage.STAGE_FIRST_DOSE_APPOINTMENT_PRIOR_DAY_REMINDER);
                    workflowStageService.createOrUpdateWorkflowStage(wfStage);
                }
            }

            // Check if 1st dose completed
            if (wfStage.getCurrStage() < WorkflowStage.STAGE_FIRST_DOSE_COMPLETED && prevFirstDoseCompletedEl != null && currFirstDoseCompletedEl != null
                && !"COMPLETED".equalsIgnoreCase(prevFirstDoseCompletedEl.getState())
                && "COMPLETED".equalsIgnoreCase(currFirstDoseCompletedEl.getState())) {
                log.debug("1st dose completed");

                // Create and save notification
                Notification ntf = notificationMessageGenerator.constructFirstDoseCompletedMessage(user.getDisplayName(), subscription, status.getVacPreviousJson(), status.getVacCurrentJson());
                notificationService.saveNotification(ntf);

                // Update workflow stage
                wfStage.setUserId(user.getUserId());
                wfStage.setCurrStage(WorkflowStage.STAGE_FIRST_DOSE_COMPLETED);
                wfStage.setStageHist(wfStage.getStageHist() + "->" + WorkflowStage.STAGE_FIRST_DOSE_COMPLETED);
                workflowStageService.createOrUpdateWorkflowStage(wfStage);
            }


            // If 2nd dose has not complete
            if (wfStage.getCurrStage() < WorkflowStage.STAGE_SECOND_DOSE_COMPLETED && prevSecondDoseEl != null && currSecondDoseEl != null) {
                // Check 2nd dose appointment for new schedule or updates
                if (!prevSecondDoseEl.getState().equalsIgnoreCase(currSecondDoseEl.getState()) && "ACTIVE".equalsIgnoreCase(currSecondDoseEl.getState())) {
                    log.debug("2nd dose appointment SCHEDULED");

                    // Create and save notification
                    Notification ntf = notificationMessageGenerator.constructSecondDoseAppointmentScheduledMessage(user.getDisplayName(), subscription, currSecondDoseEl, status.getVacPreviousJson(), status.getVacCurrentJson());
                    notificationService.saveNotification(ntf);

                    // Update workflow stage
                    wfStage.setUserId(user.getUserId());
                    wfStage.setCurrStage(WorkflowStage.STAGE_SECOND_DOSE_APPOINTMENT_SCHEDULED);
                    wfStage.setStageHist(wfStage.getStageHist() + "->" + WorkflowStage.STAGE_SECOND_DOSE_APPOINTMENT_SCHEDULED);
                    workflowStageService.createOrUpdateWorkflowStage(wfStage);

                } else if ("ACTIVE".equalsIgnoreCase(prevSecondDoseEl.getState())
                    && "ACTIVE".equalsIgnoreCase(currSecondDoseEl.getState())
                    && isAppointmentChanged(prevSecondDoseEl.getData(), currSecondDoseEl.getData())) {
                    log.debug("2nd dose appointment CHANGED");

                    // Create and save notification
                    Notification ntf = notificationMessageGenerator.constructSecondDoseAppointmentChangedMessage(user.getDisplayName(), subscription, currSecondDoseEl, status.getVacPreviousJson(), status.getVacCurrentJson());
                    notificationService.saveNotification(ntf);

                    // Update workflow stage
                    wfStage.setUserId(user.getUserId());
                    wfStage.setCurrStage(WorkflowStage.STAGE_SECOND_DOSE_APPOINTMENT_SCHEDULED);
                    wfStage.setStageHist(wfStage.getStageHist() + "->" + WorkflowStage.STAGE_SECOND_DOSE_APPOINTMENT_SCHEDULED);
                    workflowStageService.createOrUpdateWorkflowStage(wfStage);

                }

                // Check if 2nd dose appointment is tomorrow
                DataElement el = findElement(currSecondDoseEl.getData(), TEXT_APPOINTMENT_DATE);
                String appointmentDate = null;
                if (el != null)
                    appointmentDate = el.getValue();

                if ("ACTIVE".equalsIgnoreCase(prevSecondDoseEl.getState())
                    && "ACTIVE".equalsIgnoreCase(currSecondDoseEl.getState())
                    && el != null
                    && isAppointmentTomorrow(appointmentDate)) {
                    log.debug("2nd dose appointment is tomorrow, send reminder...");

                    // Create and save notification
                    Notification ntf = notificationMessageGenerator.constructSecondDoseAppointmentPriorDayReminderMessage(user.getDisplayName(), subscription, currFirstDoseEl, status.getVacPreviousJson(), status.getVacCurrentJson());
                    notificationService.saveNotification(ntf);

                    // Update workflow stage
                    wfStage.setUserId(user.getUserId());
                    wfStage.setCurrStage(WorkflowStage.STAGE_SECOND_DOSE_APPOINTMENT_PRIOR_DAY_REMINDER);
                    wfStage.setStageHist(wfStage.getStageHist() + "->" + WorkflowStage.STAGE_SECOND_DOSE_APPOINTMENT_PRIOR_DAY_REMINDER);
                    workflowStageService.createOrUpdateWorkflowStage(wfStage);
                }
            }

            // Check if 2nd dose completed
            if (wfStage.getCurrStage() < WorkflowStage.STAGE_SECOND_DOSE_COMPLETED && prevSecondDoseCompletedEl != null && currSecondDoseCompletedEl != null
                && !"COMPLETED".equalsIgnoreCase(prevSecondDoseCompletedEl.getState())
                && "COMPLETED".equalsIgnoreCase(currSecondDoseCompletedEl.getState())) {
                log.debug("2nd dose completed");

                // Create and save notification
                Notification ntf = notificationMessageGenerator.constructSecondDoseCompletedMessage(user.getDisplayName(), subscription, status.getVacPreviousJson(), status.getVacCurrentJson());
                notificationService.saveNotification(ntf);

                // Update workflow stage
                wfStage.setUserId(user.getUserId());
                wfStage.setCurrStage(WorkflowStage.STAGE_SECOND_DOSE_COMPLETED);
                wfStage.setStageHist(wfStage.getStageHist() + "->" + WorkflowStage.STAGE_SECOND_DOSE_COMPLETED);
                workflowStageService.createOrUpdateWorkflowStage(wfStage);
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

    private boolean isAppointmentTomorrow(String appointmentDateStr) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        LocalDate appointmentDate = LocalDate.parse(appointmentDateStr, formatter);
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        return appointmentDate.equals(tomorrow);
    }

    private WorkflowStage initializeWorkflowStage(String userId, StageElement currFirstDoseEl, StageElement currSecondDoseEl) {
        WorkflowStage stage = new WorkflowStage();
        stage.setUserId(userId);
        if ("COMPLETED".equalsIgnoreCase(currSecondDoseEl.getState())) {
            stage.setCurrStage(WorkflowStage.STAGE_SECOND_DOSE_COMPLETED);
        } else if ("ACTIVE".equalsIgnoreCase(currSecondDoseEl.getState())) {
            stage.setCurrStage(WorkflowStage.STAGE_SECOND_DOSE_APPOINTMENT_SCHEDULED);
        } else if ("COMPLETED".equalsIgnoreCase(currFirstDoseEl.getState())) {
            stage.setCurrStage(WorkflowStage.STAGE_FIRST_DOSE_COMPLETED);
        } else if ("ACTIVE".equalsIgnoreCase(currFirstDoseEl.getState())) {
            stage.setCurrStage(WorkflowStage.STAGE_FIRST_DOSE_APPOINTMENT_SCHEDULED);
        } else {
            stage.setCurrStage(WorkflowStage.STAGE_INIT);
        }
        stage.setStageHist("");
        stage.setLastModifiedDate(new Date());

        return stage;
    }
}
