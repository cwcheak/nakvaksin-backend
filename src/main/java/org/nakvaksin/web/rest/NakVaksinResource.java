package org.nakvaksin.web.rest;

import org.apache.commons.lang3.StringUtils;
import org.nakvaksin.domain.Subscription;
import org.nakvaksin.domain.TokenHistory;
import org.nakvaksin.service.*;
import org.nakvaksin.service.exception.AuthenticationFailedException;
import org.nakvaksin.web.rest.errors.BadRequestException;
import org.nakvaksin.web.rest.vm.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Optional;

@Path("/api/v1")
@RequestScoped
public class NakVaksinResource {
    private final Logger log = LoggerFactory.getLogger(NakVaksinResource.class);

    @Inject
    NakVaksinService nakVaksinService;

    @Inject
    SubscriptionService subscriptionService;

    @Inject
    TokenHistoryService tokenHistoryService;

    @Inject
    UserService userService;

    @Inject
    UnsubService unsubService;

    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(@HeaderParam("user-agent") String userAgent, LoginVM loginVM) {
        log.debug("Request to login by user : {}", loginVM.getUsername());
        log.debug("loginVM : {}", loginVM);

        if (StringUtils.isBlank(loginVM.getUsername())) {
            throw new BadRequestException("Username cannot be blank");
        }

        if (StringUtils.isBlank(loginVM.getPassword())) {
            throw new BadRequestException("Password cannot be blank");
        }

        UserProfileVM vm = null;
        try {
            vm = nakVaksinService.performLogin(userAgent, loginVM.getUsername(), loginVM.getPassword());

            return Response.ok().header("x-auth-token", vm.getUser().getToken()).entity(vm).build();
        } catch (AuthenticationFailedException afe) {
            return Response.status(Response.Status.UNAUTHORIZED)
                .entity(afe.getMessage())
                .build();
        }
    }

    @GET
    @Path("/profile")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getProfile(@HeaderParam("x-auth-token") String token) {
        log.debug("Request to getProfile: {}", token);

        if (StringUtils.isBlank(token)) {
            throw new BadRequestException("Token is missing");
        }

        // Get user profile
        try {
            UserElement userEl = nakVaksinService.getProfile(token);

            // Get latest token from user profile and set to response header
            var latestToken = userService.getLatestToken(userEl.getUserId());

            return Response.ok().header("x-auth-token", latestToken).entity(userEl).build();
        } catch (AuthenticationFailedException afe) {
            return Response.status(Response.Status.UNAUTHORIZED)
                .entity(afe.getMessage())
                .build();
        }
    }

    @POST
    @Path("/subscribe")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response subscribe(@HeaderParam("x-auth-token") String token, SubscriptionVM subscriptionVM) {
        log.debug("Request to subscribe.. Token: {}, Subscription: {}", token, subscriptionVM);

        if (StringUtils.isBlank(token)) {
            throw new BadRequestException("Token is missing");
        }

        if (StringUtils.isNotBlank(subscriptionVM.getUserEmail()) && !ValidationUtils.isValidEmail(subscriptionVM.getUserEmail())) {
            throw new BadRequestException("Invalid user email");
        }

        if (StringUtils.isNotBlank(subscriptionVM.getFamilyEmail()) && !ValidationUtils.isValidEmail(subscriptionVM.getFamilyEmail())) {
            throw new BadRequestException("Invalid family email");
        }

        if (StringUtils.isNotBlank(subscriptionVM.getUserPhoneNumber()) && !StringUtils.isNumeric(subscriptionVM.getUserPhoneNumber())) {
            throw new BadRequestException("Invalid user phone number");
        }

        if (StringUtils.isNotBlank(subscriptionVM.getFamilyPhoneNumber()) && !StringUtils.isNumeric(subscriptionVM.getFamilyPhoneNumber())) {
            throw new BadRequestException("Invalid family phone number");
        }

        if (StringUtils.isNotBlank(subscriptionVM.getFamilyPhoneNumber()) && StringUtils.isNotBlank(subscriptionVM.getUserPhoneNumber()) &&
            subscriptionVM.getFamilyPhoneNumber().equals(subscriptionVM.getUserPhoneNumber())) {
            throw new BadRequestException("Family phone number should not be the same as user phone number");
        }

        if (StringUtils.isNotBlank(subscriptionVM.getUserEmail()) && StringUtils.isNotBlank(subscriptionVM.getFamilyEmail()) &&
            subscriptionVM.getUserEmail().equals(subscriptionVM.getFamilyEmail())) {
            throw new BadRequestException("Family email should not be the same as user email");
        }

        // Get user from token
        TokenHistory th = tokenHistoryService.findToken(token);
        if (th == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid token").build();
        }

        // Check if unsubscribe
        if (StringUtils.isBlank(subscriptionVM.getUserPhoneNumber()) &&
            StringUtils.isBlank(subscriptionVM.getUserEmail()) &&
            StringUtils.isBlank(subscriptionVM.getFamilyPhoneNumber()) &&
            StringUtils.isBlank(subscriptionVM.getFamilyEmail())) {

            subscriptionService.removeSubscription(th.getUserId());
        } else {
            // Save subscription
            Subscription subscription = new Subscription();
            subscription.setUserId(th.getUserId());
            subscription.setUserPhoneNumber(subscriptionVM.getUserPhoneNumber());
            subscription.setUserEmail(subscriptionVM.getUserEmail());
            subscription.setFamilyPhoneNumber(subscriptionVM.getFamilyPhoneNumber());
            subscription.setFamilyEmail(subscriptionVM.getFamilyEmail());
            subscriptionService.createOrUpdateSubscription(subscription);
        }

        // Get latest token from user profile and set to response header
        var latestToken = userService.getLatestToken(th.getUserId());

        return Response.ok().header("x-auth-token", latestToken).build();
    }

    @GET
    @Path("/subscribe")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSubscription(@HeaderParam("x-auth-token") String token) {
        log.debug("Request to get subscription: Token: {}", token);

        if (StringUtils.isBlank(token)) {
            throw new BadRequestException("Token is missing");
        }

        // Get user from token
        var th = tokenHistoryService.findToken(token);
        if (th == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid token").build();
        }

        Optional<Subscription> subOpt = subscriptionService.getByUserId(th.getUserId());
        if (subOpt.isPresent()) {
            var sub = subOpt.get();
            var vm = new SubscriptionVM();
            vm.setUserPhoneNumber(sub.getUserPhoneNumber());
            vm.setUserEmail(sub.getUserEmail());
            vm.setFamilyPhoneNumber(sub.getFamilyPhoneNumber());
            vm.setFamilyEmail(sub.getFamilyEmail());

            // Get latest token from user profile and set to response header
            var latestToken = userService.getLatestToken(th.getUserId());

            return Response.ok().header("x-auth-token", latestToken).entity(vm).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND)
                .entity("Subscription for user " + th.getUserId() + " not found.")
                .build();
        }
    }

    @GET
    @Path("/vac-status")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getVaccinationStatus(@HeaderParam("x-auth-token") String token) {
        log.debug("Request to getVaccinationStatus: {}", token);

        if (StringUtils.isBlank(token)) {
            throw new BadRequestException("Token is missing");
        }

        try {
            // Get user from token
            TokenHistory th = tokenHistoryService.findToken(token);
            if (th == null) {
                throw new AuthenticationFailedException("Invalid token");
            }

            // Get latest token from user profile and set to response header
            var latestToken = userService.getLatestToken(th.getUserId());

            StageElement[] stages = nakVaksinService.getVaccinationStatusJson(latestToken);
            log.debug("Vac Status Json: {}", stages);

            return Response.ok().header("x-auth-token", latestToken).entity(stages).build();
        } catch (AuthenticationFailedException afe) {
            return Response.status(Response.Status.UNAUTHORIZED)
                .entity(afe.getMessage())
                .build();
        }
    }

    @GET
    @Path("/unsub/{key}")
    public Response unsubscribe(@PathParam("key") String unsubKey) {
        log.debug("Request to unsubscribe: {}", unsubKey);

        unsubService.unsubscribe(unsubKey);
        return Response.ok().build();
    }

    @GET
    @Path("/resub/{key}")
    public Response resubscribe(@PathParam("key") String resubKey) {
        log.debug("Request to resubscribe: {}", resubKey);

        unsubService.resubscribe(resubKey);
        return Response.ok().build();
    }
}
