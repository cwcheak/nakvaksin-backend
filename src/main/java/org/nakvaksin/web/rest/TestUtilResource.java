package org.nakvaksin.web.rest;

import io.vertx.core.eventbus.EventBus;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.nakvaksin.domain.AdaSMSResponse;
import org.nakvaksin.domain.Email;
import org.nakvaksin.domain.VaccinationStatus;
import org.nakvaksin.repository.VaccinationStatusRepository;
import org.nakvaksin.scheduler.VaccinationStatusChecker;
import org.nakvaksin.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import java.util.concurrent.ExecutionException;

@Path("/api/v1")
@RequestScoped
public class TestUtilResource {
    private final Logger log = LoggerFactory.getLogger(TestUtilResource.class);

    @Inject
    VaccinationStatusService vaccinationStatusService;

    @Inject
    VaccinationStatusRepository vaccinationStatusRepository;

    @Inject
    VaccinationStatusChecker vaccinationStatusChecker;

    @Inject
    TokenHistoryService tokenHistoryService;

    @Inject
    GmailMailerService gmailMailerService;

    @Inject
    @RestClient
    AdaSMSService adaSMSService;

    @ConfigProperty(name = "adasms-api.token")
    String adaSMSToken;

    @GET
    @Path("/testCompareVacStatus")
    public void testCompareVacStatus() {
        log.debug("Request to get testCompareVacStatus");
        try {
            VaccinationStatus status = vaccinationStatusRepository.getVaccinationStatus("F36D245FB41354E1851A2637B0247A53BB2FFD5977EF3105C67B40B737B2CB4E");
            vaccinationStatusService.compareVacStatus(status);
        } catch (ExecutionException | InterruptedException e) {
            log.error(e.getMessage());
            e.printStackTrace();
        }
    }

    @GET
    @Path("/testSendEmail")
    public void testSendEmail() {
        log.debug("Request to testSendEmail");
        try {
            Email email = new Email();
            email.setFrom("no-reply@nakvaksin.com");
            email.setTo("cwcheak.dev@gmail.com");
            email.setSubject("NakVaksin Reminder");
            //email.setBody("<p>Your 1st dose appointment has been<strong>scheduled</strong>. Please check your MySejahtera app for details.</p><p><table><tr><td>HealthFacility:</td><td>HospitalWanitadanKanak-Kanak,Likas</td></tr><tr><td>VaccinationLocation:</td><td>HOSPITALWANITADANKANAK-KANAK,LIKAS</td></tr><tr><td>Date:</td><td>22-06-2021</td></tr><tr><td>Time:</td><td>12:00PM</td></tr></table></p><p>RemembertoconfirmyourappointmentinMySejahteraapp.</p>");
            email.setBody("Your 1st dose appointment has been scheduled.");
            gmailMailerService.sendEmail(email);
            log.debug("Send email successful!");
        } catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
        }
    }

    @GET
    @Path("/testSMS")
    public void testSMS(@Context UriInfo uriInfo) {
        log.debug("Request to testSMS");
        try {
            AdaSMSResponse response = adaSMSService.sendSMS(
                adaSMSToken,
                "60164570087",
                "NakVaksin: Your 1st dose vaccination appointment has been scheduled. Please check your MySejahtera app.\n\nClick to unsubscribe: https://nakvaksin.com/r/1234567890");
            log.debug("Response : {}", response);
        } catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
        }
    }

    @GET
    @Path("/testCheckAppointment")
    public void testCheckAppointment() {
        log.debug("Request to testCheckAppointment");
        try {
            vaccinationStatusChecker.checkAppointment();
        } catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
        }
    }


    @GET
    @Path("/deleteTokenHistory")
    public void deleteTokenHistory() {
        log.debug("Request to deleteTokenHistory");
        try {
            tokenHistoryService.deleteAll();
        } catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
        }
    }
}
