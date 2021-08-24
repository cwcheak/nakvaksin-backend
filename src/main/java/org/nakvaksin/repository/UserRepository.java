package org.nakvaksin.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import org.nakvaksin.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@ApplicationScoped
public class UserRepository {
    private final Logger log = LoggerFactory.getLogger(UserRepository.class);

    @Inject
    Firestore firestore;

    public User getUserByUserId(String userId) throws ExecutionException, InterruptedException {
        log.debug("getUserByUserId : {}", userId);

        CollectionReference users = firestore.collection("users");
        DocumentReference docRef = users.document(userId);
        ApiFuture<DocumentSnapshot> future = docRef.get();
        DocumentSnapshot doc = future.get();
        if (doc.exists()) {
            User user = new User(
                doc.getId(),
                doc.getString("username"),
                doc.getString("displayName"),
                doc.getString("phoneNumber"),
                doc.getString("email"),
                doc.getString("token"),
                doc.getDate("lastModifiedDate"));
            log.debug("user : {}", user);
            return user;
        }

        return null;
    }

    public User getUserByUsername(String username) throws ExecutionException, InterruptedException {
        log.debug("getUserByUsername : {}", username);

        CollectionReference users = firestore.collection("users");
        Query query = users.whereEqualTo("username", username).limit(1);
        ApiFuture<QuerySnapshot> querySnapshot = query.get();

        if (querySnapshot.get().size() > 0) {
            DocumentSnapshot doc = querySnapshot.get().getDocuments().get(0);
            User user = new User(
                doc.getId(),
                doc.getString("username"),
                doc.getString("displayName"),
                doc.getString("phoneNumber"),
                doc.getString("email"),
                doc.getString("token"),
                doc.getDate("lastModifiedDate"));
            log.debug("user : {}", user);
            return user;
        }

        return null;
    }

    public void saveOrUpdateUserProfile(User user) throws ExecutionException, InterruptedException {
        log.debug("createOrUpdateUserProfile : {}", user);

        CollectionReference users = firestore.collection("users");
        DocumentReference docRef = users.document(user.getUserId());
        ApiFuture<DocumentSnapshot> future = docRef.get();
        DocumentSnapshot doc = future.get();
        if (doc.exists()) {
            log.debug("User exists. Updating...");
            firestore.runTransaction(transaction -> {
                transaction.update(docRef, "username", user.getUsername());
                transaction.update(docRef, "displayName", user.getDisplayName());
                transaction.update(docRef, "phoneNumber", user.getPhoneNumber());
                transaction.update(docRef, "email", user.getEmail());
                transaction.update(docRef, "token", user.getToken());
                transaction.update(docRef, "lastModifiedDate", new Date());
                return null;
            });
        } else {
            log.debug("User does not exists. Create new...");
            Map<String, Object> newUser = new HashMap<>();
            newUser.put("username", user.getUsername());
            newUser.put("displayName", user.getDisplayName());
            newUser.put("phoneNumber", user.getPhoneNumber());
            newUser.put("email", user.getEmail());
            newUser.put("token", user.getToken());
            newUser.put("lastModifiedDate", new Date());
            docRef.set(newUser);
        }
    }

    public void trackUserAgent(String username, String userAgent) throws ExecutionException, InterruptedException {
        log.debug("trackUserAgent : {} : {}", username, userAgent);

        CollectionReference userAgents = firestore.collection("user-agents");

        DocumentReference docRef = userAgents.document();
        Map<String, Object> newUserAgent = new HashMap<>();
        newUserAgent.put("userId", username);
        newUserAgent.put("userAgent", userAgent);
        newUserAgent.put("timestamp", new Date());
        docRef.set(newUserAgent);
    }
}
