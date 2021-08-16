package org.nakvaksin.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import org.nakvaksin.domain.VaccinationStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@ApplicationScoped
public class VaccinationStatusRepository {
    private final Logger log = LoggerFactory.getLogger(VaccinationStatusRepository.class);

    @Inject
    Firestore firestore;

    public VaccinationStatus saveOrUpdateVaccinationStatus(VaccinationStatus currVacStatus) throws ExecutionException, InterruptedException {
        log.debug("createOrUpdateVaccinationStatus : {}", currVacStatus);

        CollectionReference db = firestore.collection("vaccination_status");
        DocumentReference docRef = db.document(currVacStatus.getUserId());
        ApiFuture<DocumentSnapshot> future = docRef.get();
        DocumentSnapshot doc = future.get();
        if (doc.exists()) {
            log.debug("VaccinationStatus exists. Updating...");
            ApiFuture<VaccinationStatus> futureTransaction = firestore.runTransaction(transaction -> {
                Date currDate = new Date();
                DocumentSnapshot snapshot = transaction.get(docRef).get();
                transaction.update(docRef, "vacPreviousJson", doc.getString("vacCurrentJson"));
                transaction.update(docRef, "vacPreviousDate", doc.getDate("vacCurrentDate"));
                transaction.update(docRef, "vacCurrentJson", currVacStatus.getVacCurrentJson());
                transaction.update(docRef, "vacCurrentDate", currDate);

                VaccinationStatus newStatus = new VaccinationStatus();
                newStatus.setUserId(currVacStatus.getUserId());
                newStatus.setVacPreviousJson(doc.getString("vacCurrentJson"));
                newStatus.setVacPreviousDate(doc.getDate("vacCurrentDate"));
                newStatus.setVacCurrentJson(currVacStatus.getVacCurrentJson());
                newStatus.setVacCurrentDate(currDate);
                return newStatus;
            });

            return futureTransaction.get();
        } else {
            log.debug("VaccinationStatus does not exists. Create new...");
            Date currDate = new Date();
            Map<String, Object> newVacStatus = new HashMap<>();
            newVacStatus.put("vacCurrentJson", currVacStatus.getVacCurrentJson());
            newVacStatus.put("vacCurrentDate", currDate);
            docRef.set(newVacStatus);

            VaccinationStatus newStatus = new VaccinationStatus();
            newStatus.setVacCurrentJson(currVacStatus.getVacCurrentJson());
            newStatus.setVacCurrentDate(currDate);
            return newStatus;
        }
    }

    public VaccinationStatus getVaccinationStatus(String userId) throws ExecutionException, InterruptedException {
        log.debug("getVaccinationStatus. User Id: {}", userId);

        CollectionReference db = firestore.collection("vaccination_status");
        DocumentReference docRef = db.document(userId);
        ApiFuture<DocumentSnapshot> future = docRef.get();
        DocumentSnapshot doc = future.get();
        if (doc.exists()) {
            VaccinationStatus newStatus = new VaccinationStatus();
            newStatus.setUserId(userId);
            newStatus.setVacCurrentJson(doc.getString("vacCurrentJson"));
            newStatus.setVacCurrentDate(doc.getDate("vacCurrentDate"));
            newStatus.setVacPreviousJson(doc.getString("vacPreviousJson"));
            newStatus.setVacPreviousDate(doc.getDate("vacPreviousDate"));
            return newStatus;
        }

        return null;
    }


    /*
    public void getSubscribedUser() throws ExecutionException, InterruptedException {
        log.debug("getSubscribedUser");

        CollectionReference db = firestore.collection("vaccination_status");
        Query query = db.whereEqualTo("isOptIn", true);
        ApiFuture<QuerySnapshot> querySnapshot = query.get();

        if (querySnapshot.get().size() > 0) {
            DocumentSnapshot doc = querySnapshot.get().getDocuments().get(0);
            User user = new User(
                doc.getId(),
                doc.getString("username"),
                doc.getString("displayName"),
                doc.getString("phoneNumber"),
                doc.getString("token"),
                DateUtils.convertToLocalDateTime(doc.getDate("lastModifiedDate")));
            log.debug("user : {}", user);
            // return user;
        }
    }
     */
}
