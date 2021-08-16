package org.nakvaksin.service;

import io.quarkus.cache.CacheInvalidate;
import io.quarkus.cache.CacheResult;
import io.quarkus.vertx.ConsumeEvent;
import io.smallrye.common.annotation.Blocking;
import org.nakvaksin.domain.TokenHistory;
import org.nakvaksin.domain.User;
import org.nakvaksin.repository.UserRepository;
import org.nakvaksin.service.exception.DBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Date;
import java.util.concurrent.ExecutionException;

@ApplicationScoped
public class UserService {
    private final Logger log = LoggerFactory.getLogger(UserService.class);

    @Inject
    UserRepository userRepository;

    @Inject
    TokenHistoryService tokenHistoryService;

    @ConsumeEvent("update-user-profile-channel")
    @Blocking
    void createOrUpdateUserProfile(User user) {
        log.debug("createOrUpdateUserProfile : {}", user);

        try {
            invalidateLatestUserTokenCache(user.getUserId());
            userRepository.saveOrUpdateUserProfile(user);

            TokenHistory th = tokenHistoryService.findToken(user.getToken());
            if(th == null) {
                TokenHistory newTH = new TokenHistory(user.getToken(), user.getUserId(), new Date());
                tokenHistoryService.saveToken(newTH);
            }
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    @ConsumeEvent("track-user-agent-channel")
    @Blocking
    void trackUserAgent(String input) {
        log.debug("trackUserAgent : {}", input);

        try {
            String[] data = input.split("\\|");
            userRepository.trackUserAgent(data[0], data[1]);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public User getUserByUserId(String userId) {
        log.debug("getUserByUserId : {}", userId);

        try {
            return userRepository.getUserByUserId(userId);
        } catch (Exception e) {
            throw new DBException("Error getting user by user id");
        }
    }

    @CacheResult(cacheName = "latest-user-token")
    public String getLatestToken(String userId) {
        log.debug("getLatestToken : {}", userId);

        try {
            var user = userRepository.getUserByUserId(userId);
            return user.getToken();
        } catch (Exception e) {
            throw new DBException("Error getting user token");
        }
    }

    @CacheInvalidate(cacheName = "latest-user-token")
    public void invalidateLatestUserTokenCache(String userId) {
    }
}
