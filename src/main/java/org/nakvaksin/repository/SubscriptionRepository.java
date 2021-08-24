package org.nakvaksin.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import org.nakvaksin.domain.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@ApplicationScoped
public class SubscriptionRepository {
    private final Logger log = LoggerFactory.getLogger(SubscriptionRepository.class);

    private final int PAGE_SIZE = 20;

    @Inject
    Firestore firestore;

    public Subscription getSubscriptionByUserId(String userId) throws ExecutionException, InterruptedException {
        log.debug("getSubscriptionByUserId : {}", userId);
        CollectionReference subscriptions = firestore.collection("subscriptions");

        DocumentReference docRef = subscriptions.document(userId);
        ApiFuture<DocumentSnapshot> future = docRef.get();
        DocumentSnapshot doc = future.get();
        if (doc.exists()) {
            var subscription = new Subscription();
            subscription.setUserId(doc.getId());
            subscription.setUserPhoneNumber(doc.getString("userPhoneNumber"));
            subscription.setUserEmail(doc.getString("userEmail"));
            subscription.setFamilyPhoneNumber(doc.getString("familyPhoneNumber"));
            subscription.setFamilyEmail(doc.getString("familyEmail"));
            subscription.setCreatedDate(doc.getDate("createdDate"));
            subscription.setLastModifiedDate(doc.getDate("lastModifiedDate"));
            return subscription;
        } else {
            return null;
        }
    }

    public void saveOrUpdateSubscription(Subscription subscription) throws ExecutionException, InterruptedException {
        log.debug("createOrUpdateSubscription : {}", subscription);

        CollectionReference subscriptions = firestore.collection("subscriptions");
        DocumentReference docRef = subscriptions.document(subscription.getUserId());
        ApiFuture<DocumentSnapshot> future = docRef.get();
        DocumentSnapshot doc = future.get();
        if (doc.exists()) {
            log.debug("Subscription exists. Updating...");
            ApiFuture<Void> futureTransaction = firestore.runTransaction(transaction -> {
                transaction.update(docRef, "userPhoneNumber", subscription.getUserPhoneNumber());
                transaction.update(docRef, "userEmail", subscription.getUserEmail());
                transaction.update(docRef, "familyPhoneNumber", subscription.getFamilyPhoneNumber());
                transaction.update(docRef, "familyEmail", subscription.getFamilyEmail());
                transaction.update(docRef, "lastModifiedDate", new Date());
                return null;
            });
        } else {
            log.debug("Subscription does not exists. Create new...");
            Date now = new Date();
            Map<String, Object> newSub = new HashMap<>();
            newSub.put("userPhoneNumber", subscription.getUserPhoneNumber());
            newSub.put("userEmail", subscription.getUserEmail());
            newSub.put("familyPhoneNumber", subscription.getFamilyPhoneNumber());
            newSub.put("familyEmail", subscription.getFamilyEmail());
            newSub.put("createdDate", now);
            newSub.put("lastModifiedDate", now);
            docRef.set(newSub);
        }
    }

    public void removeSubscription(String userId) throws ExecutionException, InterruptedException {
        log.debug("removeSubscription : {}", userId);

        CollectionReference db = firestore.collection("subscriptions");
        DocumentReference docRef = db.document(userId);
        ApiFuture<WriteResult> writeResult = docRef.delete();
        writeResult.get();
    }


    public List<Subscription> getAll() throws ExecutionException, InterruptedException, TimeoutException {
        log.debug("getAll");
        int page = 1;

        CollectionReference db = firestore.collection("subscriptions");
        Query firstPageQuery = db.orderBy("createdDate", Query.Direction.ASCENDING).limit(PAGE_SIZE);
        ApiFuture<QuerySnapshot> querySnapshot = firstPageQuery.get();
        List<QueryDocumentSnapshot> docs = querySnapshot.get(60, TimeUnit.SECONDS).getDocuments();
        QueryDocumentSnapshot lastDoc = docs.get(docs.size() - 1);

        List<Subscription> subscriptions = new ArrayList<Subscription>();
        subscriptions.addAll(toSubscription(docs));
        log.debug("Page: 1");

        while (docs.size() == PAGE_SIZE) {
            page++;
            log.debug("Page: {}", page);
            Query subsequentPageQuery = db.orderBy("createdDate", Query.Direction.ASCENDING)
                    .startAfter(lastDoc)
                    .limit(PAGE_SIZE);

            querySnapshot = subsequentPageQuery.get();
            docs = querySnapshot.get(60, TimeUnit.SECONDS).getDocuments();
            lastDoc = docs.get(docs.size() - 1);
            subscriptions.addAll(toSubscription(docs));
        }

        return subscriptions;
    }

    private List<Subscription> toSubscription(List<QueryDocumentSnapshot> docs) {
        log.debug("toSubscription - doc size : {}", docs.size());
        List<Subscription> subs = new ArrayList<Subscription>();
        for (int i = 0; i < docs.size(); i++) {
            QueryDocumentSnapshot doc = docs.get(i);
            Subscription s = new Subscription();
            s.setUserId(doc.getId());
            s.setUserPhoneNumber(doc.getString("userPhoneNumber"));
            s.setUserEmail(doc.getString("userEmail"));
            s.setFamilyPhoneNumber(doc.getString("familyPhoneNumber"));
            s.setFamilyEmail(doc.getString("familyEmail"));
            s.setCreatedDate(doc.getDate("createdDate"));
            s.setLastModifiedDate(doc.getDate("lastModifiedDate"));
            subs.add(s);
        }

        return subs;
    }
}
