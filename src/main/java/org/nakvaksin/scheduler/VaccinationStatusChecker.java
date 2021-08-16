package org.nakvaksin.scheduler;

import io.quarkus.scheduler.Scheduled;
import io.vertx.core.eventbus.EventBus;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.nakvaksin.domain.Subscription;
import org.nakvaksin.domain.VaccinationStatus;
import org.nakvaksin.service.MySejahteraService;
import org.nakvaksin.service.SubscriptionService;
import org.nakvaksin.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.util.List;

@ApplicationScoped
public class VaccinationStatusChecker {
    private final Logger log = LoggerFactory.getLogger(VaccinationStatusChecker.class);

    @Inject
    EventBus eventBus;

    @Inject
    UserService userService;

    @Inject
    SubscriptionService subscriptionService;

    @Inject
    @RestClient
    MySejahteraService mySejahteraService;

    @Scheduled(cron = "{cron.expr.appointmentChecker}")
    public void checkAppointment() {
        log.debug("[Scheduler] checkAppointment...");

        try {
            List<Subscription> subs = subscriptionService.getAll();
            log.debug("subs length: {}", subs.size());

            for (Subscription sub : subs) {
                // Get user
                var user = userService.getUserByUserId(sub.getUserId());
                if (user != null) {
                    log.debug("Checking vac status for user {}", user.getUserId());

                    // Get and update user's vaccination status in db
                    Response res = mySejahteraService.getVaccinationStatus(user.getToken());
                    String vacStatus = res.readEntity(String.class);
                    log.debug("vacStatus: {}", vacStatus);
                    eventBus.send("update-vaccination-status-channel", VaccinationStatus.builder().userId(user.getUserId()).vacCurrentJson(vacStatus).build());

                    // Get and update user's token in db
                    MultivaluedMap<String, String> headers = res.getStringHeaders();
                    String token = headers.get("X-AUTH-TOKEN").get(0);
                    log.debug("token: {}", token);
                    user.setToken(token);
                    eventBus.send("update-user-profile-channel", user);
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
        }
    }
}
