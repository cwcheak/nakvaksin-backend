package org.nakvaksin.service;

import io.quarkus.cache.CacheInvalidate;
import io.quarkus.cache.CacheResult;
import org.apache.commons.lang3.RandomStringUtils;
import org.nakvaksin.domain.Subscription;
import org.nakvaksin.domain.User;
import org.nakvaksin.repository.SubscriptionRepository;
import org.nakvaksin.repository.UserRepository;
import org.nakvaksin.service.exception.DBException;
import org.nakvaksin.service.exception.SubscriptionNotFoundException;
import org.nakvaksin.service.exception.UserNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@ApplicationScoped
@Transactional
public class SubscriptionService {
    private final Logger log = LoggerFactory.getLogger(SubscriptionService.class);

    @Inject
    SubscriptionRepository subscriptionRepository;

    @Inject
    UserRepository userRepository;

    public void createOrUpdateSubscription(Subscription subscription) {
        log.debug("Request to create or update Subscription : {}", subscription);

        try {
            invalidateSubscriptionCache(subscription.getUserId());

            // Find out user id from username
            var user = userRepository.getUserByUserId(subscription.getUserId());
            if (user != null) {
                subscriptionRepository.saveOrUpdateSubscription(subscription);
            } else {
                throw new UserNotFoundException("User " + subscription.getUserId() + " not found.");
            }
        } catch (ExecutionException | InterruptedException e) {
            throw new DBException("Error create or update subscription");
        }
    }

    public void removeSubscription(String userId) {
        log.debug("Request to remove Subscription by user id : {}", userId);

        try {
            invalidateSubscriptionCache(userId);
            subscriptionRepository.removeSubscription(userId);
        } catch (Exception e) {
            throw new DBException("Error removing subscription");
        }
    }

    public Optional<Subscription> getByUserId(String userId) {
        log.debug("Request to get Subscription by user id : {}", userId);

        try {
            return getByUserIdInternal(userId);
        } catch (SubscriptionNotFoundException e) {
            return Optional.empty();
        } catch (Exception e) {
            throw new DBException("Error retrieving subscription");
        }
    }

    @CacheResult(cacheName = "subscription")
    public Optional<Subscription> getByUserIdInternal(String userId) throws ExecutionException, InterruptedException {
        log.debug("Internal Request to get Subscription by user id : {}", userId);

        Optional<Subscription> subOpt = Optional.ofNullable(subscriptionRepository.getSubscriptionByUserId(userId));
        if (!subOpt.isPresent()) {
            throw new SubscriptionNotFoundException("Subscription not found");
        }
        return subOpt;
    }

    public Optional<Subscription> getByUsername(String username) {
        log.debug("Request to get Subscription by username : {}", username);

        try {
            // Find out user id from username
            var user = userRepository.getUserByUsername(username);
            if (user != null) {
                return getByUserId(user.getUserId());
            } else {
                return Optional.empty();
            }
        } catch (Exception e) {
            throw new DBException("Error retrieving subscription");
        }
    }

    public List<Subscription> getAll() {
        try {
            return subscriptionRepository.getAll();
        } catch (Exception e) {
            e.printStackTrace();
            throw new DBException("Error retrieving subscription");
        }
    }

    @CacheInvalidate(cacheName = "subscription")
    public void invalidateSubscriptionCache(String userId) {
    }
}
