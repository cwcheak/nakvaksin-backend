package org.nakvaksin.scheduler;

import io.quarkus.scheduler.Scheduled;
import org.nakvaksin.domain.TokenHistory;
import org.nakvaksin.service.TokenHistoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Calendar;
import java.util.List;

@ApplicationScoped
public class TokenHistoryPurgeTask {
    private final Logger log = LoggerFactory.getLogger(TokenHistoryPurgeTask.class);

    @Inject
    TokenHistoryService tokenHistoryService;

    @Scheduled(cron = "{cron.expr.purgeToken}")
    public void purgeToken() {
        log.debug("[Scheduler] purgeToken...");

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -90);
        log.debug("Date older than 90 days : {}", calendar.toString());

        List<TokenHistory> tokens = tokenHistoryService.findTokensOlderThan(calendar.getTime());
        log.debug("There are {} tokens to delete.", tokens.size());
        tokens.stream().forEach(token -> {
            tokenHistoryService.deleteToken(token);
            log.debug("Token deleted : {}", token.getToken());
        });
    }
}
