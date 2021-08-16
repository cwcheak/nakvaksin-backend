package org.nakvaksin.service;

import org.apache.commons.lang3.RandomStringUtils;
import org.nakvaksin.domain.UnSub;
import org.nakvaksin.repository.UnsubRepository;
import org.nakvaksin.service.exception.DBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.concurrent.ExecutionException;

@ApplicationScoped
public class UnsubService {
    private final Logger log = LoggerFactory.getLogger(UnsubService.class);

    @Inject
    UnsubRepository unsubRepository;

    public UnSub createNewUnsubForContact(String contact) {
        log.debug("createNewUnsubForContact: {}", contact);
        try {
            String generatedKey = generateUnsubKey();
            while (unsubRepository.isKeyExists(generatedKey)) {
                generatedKey = generateUnsubKey();
            }

            UnSub unsub = new UnSub(generatedKey, contact, false);
            unsubRepository.saveUnsub(unsub);
            return unsub;
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            throw new DBException("Error creating unsub for contact");
        }
    }

    public UnSub getUnsubForContact(String contact) {
        log.debug("getUnsubForContact: {}", contact);

        try {
            return unsubRepository.getUnsubForContact(contact);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            throw new DBException("Error checking if contact has unsubscribed");
        }
    }

    public boolean isContactUnsub(String contact) {
        log.debug("isContactUnsub: {}", contact);

        try {
            return unsubRepository.isContactUnsub(contact);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            throw new DBException("Error checking if contact has unsubscribed");
        }
    }

    public boolean isRecordForContactExists(String contact) {
        log.debug("isRecordForContactExists: {}", contact);

        try {
            return unsubRepository.isRecordForContactExists(contact);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            throw new DBException("Error checking if unsub record for contact exists");
        }
    }

    public void unsubscribe(String key) {
        log.debug("unsubscribe: {}", key);

        try {
            unsubRepository.updateUnsubStatus(key, true);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            throw new DBException("Error when unsubscribe");
        }
    }

    public void resubscribe(String key) {
        log.debug("resubscribe: {}", key);

        try {
            unsubRepository.updateUnsubStatus(key, false);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            throw new DBException("Error when resubscribe");
        }
    }

    private String generateUnsubKey() {
        return RandomStringUtils.random(6, true, true);
    }
}
