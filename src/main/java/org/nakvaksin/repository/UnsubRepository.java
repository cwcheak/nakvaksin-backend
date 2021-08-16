package org.nakvaksin.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import org.nakvaksin.domain.UnSub;
import org.nakvaksin.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Date;
import java.util.concurrent.ExecutionException;

@ApplicationScoped
public class UnsubRepository {
    private final Logger log = LoggerFactory.getLogger(UnsubRepository.class);

    @Inject
    Firestore firestore;

    public UnSub getUnsubForContact(String contact) throws ExecutionException, InterruptedException {
        log.debug("getUnsubForContact : {}", contact);

        CollectionReference db = firestore.collection("unsub");
        Query query = db.whereEqualTo("contact", contact).limit(1);
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        if (querySnapshot.get().size() > 0) {
            return querySnapshot.get().getDocuments().get(0).toObject(UnSub.class);
        }
        return null;
    }

    public void saveUnsub(UnSub unSub) throws ExecutionException, InterruptedException {
        log.debug("saveUnsub : {}", unSub);

        DocumentReference docRef = firestore.collection("unsub").document(unSub.getKey());
        ApiFuture<WriteResult> future = docRef.set(unSub);
        future.get();
    }

    public void updateUnsubStatus(String key, boolean status) throws ExecutionException, InterruptedException {
        DocumentReference docRef = firestore.collection("unsub").document(key);
        DocumentSnapshot doc = docRef.get().get();
        if (doc.exists()) {
            firestore.runTransaction(transaction -> {
                transaction.update(docRef, "unsub", status);
                return null;
            });
        }
    }

    public boolean isContactUnsub(String contact) throws ExecutionException, InterruptedException {
        log.debug("isContactUnsub : {}", contact);

        CollectionReference db = firestore.collection("unsub");
        Query query = db.whereEqualTo("contact", contact).whereEqualTo("unsub", true).limit(1);
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        if (querySnapshot.get().size() > 0) {
            return true;
        }
        return false;
    }

    public boolean isKeyExists(String key) throws ExecutionException, InterruptedException {
        log.debug("isKeyExists : {}", key);

        DocumentReference docRef = firestore.collection("unsub").document(key);
        if (docRef.get().get().exists()) {
            return true;
        }
        return false;
    }

    public boolean isRecordForContactExists(String contact) throws ExecutionException, InterruptedException {
        log.debug("isRecordForContactExists : {}", contact);

        CollectionReference db = firestore.collection("unsub");
        Query query = db.whereEqualTo("contact", contact).limit(1);
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        if (querySnapshot.get().size() > 0) {
            return true;
        }
        return false;
    }
}
