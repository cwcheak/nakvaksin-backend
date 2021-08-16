package org.nakvaksin.service;

import io.quarkus.cache.CacheInvalidate;
import io.quarkus.cache.CacheResult;
import io.quarkus.vertx.ConsumeEvent;
import io.smallrye.common.annotation.Blocking;
import org.nakvaksin.domain.TokenHistory;
import org.nakvaksin.repository.TokenHistoryRepository;
import org.nakvaksin.service.exception.DBException;
import org.nakvaksin.service.exception.TokenHistoryNotFoundException;
import org.nakvaksin.web.rest.NakVaksinResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class TokenHistoryService {
    private final Logger log = LoggerFactory.getLogger(TokenHistoryService.class);

    @Inject
    TokenHistoryRepository tokenHistoryRepository;

    public TokenHistory findToken(String token) {
        log.debug("findToken : {}", token);

        try {
            return findTokenInternal(token);
        } catch (TokenHistoryNotFoundException thnf) {
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            throw new DBException("Error when finding token");
        }
    }

    // Workaround for Quarkus caching NULL value
    @CacheResult(cacheName = "token-history")
    public TokenHistory findTokenInternal(String token) throws NoSuchAlgorithmException, ExecutionException, InterruptedException {
        TokenHistory th = tokenHistoryRepository.findToken(token);
        if (th == null) {
            throw new TokenHistoryNotFoundException("Token History is empty");
        }
        return th;
    }

    @ConsumeEvent("save-token-channel")
    @Blocking
    void saveToken(TokenHistory tokenHistory) {
        log.debug("saveToken : {}", tokenHistory);
        try {
            var existingTH = findToken(tokenHistory.getToken());
            if (existingTH == null) {
                tokenHistoryRepository.saveToken(tokenHistory.getToken(), tokenHistory.getUserId());
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new DBException("Error when saving token");
        }
    }

    public void deleteToken(TokenHistory th) {
        log.debug("deleteToken : {}", th);
        try {
            invalidateTokenHistory(th.getToken());
            tokenHistoryRepository.deleteToken(th);
        } catch (Exception e) {
            e.printStackTrace();
            throw new DBException("Error when saving token");
        }
    }

    public void deleteAll() {
        log.debug("deleteAll");
        try {
            tokenHistoryRepository.deleteAll();
        } catch (Exception e) {
            e.printStackTrace();
            throw new DBException("Error when saving token");
        }
    }

    public List<TokenHistory> findTokensOlderThan(Date date) {
        log.debug("findTokensOlderThan");
        try {
            return tokenHistoryRepository.findTokensOlderThan(date);
        } catch (Exception e) {
            e.printStackTrace();
            throw new DBException("Error finding tokens older than");
        }
    }

    @CacheInvalidate(cacheName = "token-history")
    public void invalidateTokenHistory(String token) {
    }
}
