package org.nakvaksin.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.vertx.core.eventbus.EventBus;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.nakvaksin.domain.Subscription;
import org.nakvaksin.domain.TokenHistory;
import org.nakvaksin.domain.User;
import org.nakvaksin.domain.VaccinationStatus;
import org.nakvaksin.service.exception.AuthenticationFailedException;
import org.nakvaksin.service.exception.MySejahteraServiceException;
import org.nakvaksin.web.rest.vm.StageElement;
import org.nakvaksin.web.rest.vm.UserElement;
import org.nakvaksin.web.rest.vm.UserProfileVM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class NakVaksinService {
    private final Logger log = LoggerFactory.getLogger(NakVaksinService.class);

    @Inject
    @RestClient
    MySejahteraService mySejahteraService;

    @Inject
    SubscriptionService subscriptionService;

    @Inject
    TokenHistoryService tokenHistoryService;

    @Inject
    UserService userService;

    @Inject
    EventBus eventBus;

    public UserProfileVM performLogin(String userAgent, String username, String password) {
        log.info("Performing login...");

        try {
            Response tokenRes = mySejahteraService.login(username, password);
            MultivaluedMap<String, String> headers = tokenRes.getStringHeaders();
            String token = headers.get("X-AUTH-TOKEN").get(0);
            log.debug("username: {}, token: {}", username, token);

            // Get user profile
            String profile = mySejahteraService.getUserProfile(token);
            log.debug("profile: {}", profile);

            // Get user's vaccination status
            Response res = mySejahteraService.getVaccinationStatus(token);
            String vacStatus = res.readEntity(String.class);
            log.debug("vacStatus: {}", vacStatus);

            // Construct UserProfileVM
            var vm = new UserProfileVM();
            ObjectMapper mapper = new ObjectMapper();

            // User
            final ObjectNode node = mapper.readValue(profile, ObjectNode.class);

            User user = null;
            if (node.has("employeeInfo")) {
                user = mapper.readValue(node.get("employeeInfo").toString(), User.class);
                user.setToken(token);

                vm.getUser().setUserId(user.getUserId());
                vm.getUser().setUsername(user.getUsername());
                vm.getUser().setDisplayName(user.getDisplayName());
                vm.getUser().setPhoneNumber(user.getPhoneNumber());
                vm.getUser().setEmail(user.getEmail());
                vm.getUser().setToken(token);
            }

            // VacStatus
            StageElement[] stageElements = mapper.readValue(vacStatus, StageElement[].class);
            vm.addStages(stageElements);

            // Subscription
            Optional<Subscription> subOpt = subscriptionService.getByUserId(user.getUserId());
            if (subOpt.isPresent()) {
                log.debug("subscription: {}", subOpt.get());
                Subscription sub = subOpt.get();
                vm.getSubscription().setUserPhoneNumber(sub.getUserPhoneNumber());
                vm.getSubscription().setUserEmail(sub.getUserEmail());
                vm.getSubscription().setFamilyPhoneNumber(sub.getFamilyPhoneNumber());
                vm.getSubscription().setFamilyEmail(sub.getFamilyEmail());
            }

            // Update data
            if (user != null) {
                eventBus.send("track-user-agent-channel", user.getUserId() + "|" + userAgent);
                eventBus.send("save-token-channel", new TokenHistory(token, user.getUserId(), null));
                eventBus.send("update-user-profile-channel", user);
                eventBus.send("update-vaccination-status-channel", VaccinationStatus.builder().userId(user.getUserId()).vacCurrentJson(vacStatus).build());
            }

            log.info("Login completed");
            return vm;
        } catch (JsonProcessingException e) {
            throw new MySejahteraServiceException(e.toString());
        }
    }

    public UserElement getProfile(String token) {
        log.info("getProfile...");

        // Get user from token
        TokenHistory th = tokenHistoryService.findToken(token);
        if (th == null) {
            throw new AuthenticationFailedException("Invalid token");
        }

        // Get latest token from user profile and set to response header
        var latestToken = userService.getLatestToken(th.getUserId());

        // Get user profile
        String profile = mySejahteraService.getUserProfile(latestToken);
        log.debug("profile: {}", profile);

        ObjectMapper mapper = new ObjectMapper();
        final ObjectNode node;
        try {
            node = mapper.readValue(profile, ObjectNode.class);
            if (node.has("employeeInfo")) {
                User user = mapper.readValue(node.get("employeeInfo").toString(), User.class);
                var userEl = new UserElement();
                userEl.setUserId(user.getUserId());
                userEl.setUsername(user.getUsername());
                userEl.setDisplayName(user.getDisplayName());
                userEl.setPhoneNumber(user.getPhoneNumber());
                userEl.setEmail(user.getEmail());
                userEl.setEmail(user.getEmail());
                return userEl;
            } else {
                throw new MySejahteraServiceException("Invalid response");
            }
        } catch (JsonProcessingException e) {
            throw new MySejahteraServiceException(e.toString());
        }
    }

    public StageElement[] getVaccinationStatusJson(String token) {
        log.info("getVaccinationStatus...");

        // Get user from token
        TokenHistory th = tokenHistoryService.findToken(token);
        if (th == null) {
            throw new AuthenticationFailedException("Invalid token");
        }

        // Get latest token from user profile and set to response header
        var latestToken = userService.getLatestToken(th.getUserId());

        // Get user's vaccination status
        Response res = mySejahteraService.getVaccinationStatus(latestToken);
        String vacStatus = res.readEntity(String.class);
        log.debug("vacStatus: {}", vacStatus);

        ObjectMapper mapper = new ObjectMapper();
        final ObjectNode node;
        try {
            // return mapper.readValue(vacStatus, String.class);
            return mapper.readValue(vacStatus, StageElement[].class);
            // return stageElements;
        } catch (JsonProcessingException e) {
            throw new MySejahteraServiceException(e.toString());
        }
    }
}
