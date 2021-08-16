package org.nakvaksin.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import org.nakvaksin.domain.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@ApplicationScoped
public class NotificationRepository {
    private final Logger log = LoggerFactory.getLogger(NotificationRepository.class);

    @Inject
    Firestore firestore;

    public void saveNotification(Notification ntf) throws ExecutionException, InterruptedException {
        log.debug("saveNotification");

        CollectionReference db = firestore.collection("notifications");
        ApiFuture<WriteResult> future = db.document().set(ntf);
        future.get();
    }

    public List<Notification> getAllUnsend() throws ExecutionException, InterruptedException {
        log.debug("getAllUnsend");

        CollectionReference db = firestore.collection("notifications");
        ApiFuture<QuerySnapshot> future = db.whereEqualTo("allSent", false).get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        List<Notification> ntfs = new ArrayList<>();
        for (DocumentSnapshot document : documents) {
            Notification ntf = document.toObject(Notification.class);
            ntf.setId(document.getId());
            ntfs.add(ntf);
        }
        return ntfs;
    }

    public void updateNotification(Notification ntf) throws ExecutionException, InterruptedException {
        log.debug("updateNotification");

        DocumentReference docRef = firestore.collection("notifications").document(ntf.getId());

        ApiFuture<Void> futureTransaction = firestore.runTransaction(transaction -> {
            DocumentSnapshot snapshot = transaction.get(docRef).get();
            transaction.set(docRef, ntf);
            return null;
        });
    }
}
