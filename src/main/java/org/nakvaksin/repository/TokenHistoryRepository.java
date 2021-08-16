package org.nakvaksin.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import org.nakvaksin.domain.TokenHistory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ExecutionException;

@ApplicationScoped
public class TokenHistoryRepository {
    private final Logger log = LoggerFactory.getLogger(TokenHistoryRepository.class);

    @Inject
    Firestore firestore;

    public void saveToken(String token, String userId) throws ExecutionException, InterruptedException, NoSuchAlgorithmException {
        log.debug("saveToken : [{}] [{}]", userId, token);

        String hashedToken = hashAndBase64(token);
        log.debug("hashedToken : {}", hashedToken);

        CollectionReference db = firestore.collection("token_history");
        DocumentReference docRef = db.document();

        Map<String, Object> newUser = new HashMap<>();
        newUser.put("token", token);
        newUser.put("hashedToken", hashedToken);
        newUser.put("userId", userId);
        newUser.put("createdDate", new Date());
        docRef.set(newUser);
    }

    public TokenHistory findToken(String token) throws ExecutionException, InterruptedException, NoSuchAlgorithmException {
        log.debug("findToken : {}", token);

        String hashedToken = hashAndBase64(token);
        log.debug("hashedToken : {}", hashedToken);

        CollectionReference db = firestore.collection("token_history");
        Query query = db.whereEqualTo("hashedToken", hashedToken);
        ApiFuture<QuerySnapshot> querySnapshot = query.get();

        if (querySnapshot.get().size() > 0) {
            DocumentSnapshot doc = querySnapshot.get().getDocuments().get(0);
            TokenHistory th = doc.toObject(TokenHistory.class);
            log.debug("TokenHistory : {}", th);
            return th;
        }

        return null;
    }

    public List<TokenHistory> findTokensOlderThan(Date date) throws ExecutionException, InterruptedException {
        log.debug("findTokenOlderThan : {}", date);

        CollectionReference db = firestore.collection("token_history");
        Query query = db.whereLessThan("createdDate", date);
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        List<TokenHistory> tokenHistList = new ArrayList<>();

        for (DocumentSnapshot doc : querySnapshot.get().getDocuments()) {
            tokenHistList.add(doc.toObject(TokenHistory.class));
        }

        return tokenHistList;
    }

    public void deleteToken(TokenHistory th) throws NoSuchAlgorithmException, ExecutionException, InterruptedException {
        log.debug("deleteToken");

        String hashedToken = hashAndBase64(th.getToken());
        log.debug("hashedToken : {}", hashedToken);

        CollectionReference db = firestore.collection("token_history");
        Query query = db.whereEqualTo("hashedToken", hashedToken).limit(1);
        ApiFuture<QuerySnapshot> querySnapshot = query.get();

        if (querySnapshot.get().size() > 0) {
            DocumentSnapshot doc = querySnapshot.get().getDocuments().get(0);
            doc.getReference().delete();
        }
    }

    public void deleteAll() {
        log.debug("deleteAll");

        CollectionReference db = firestore.collection("token_history");
        Iterable<DocumentReference> docRefIt = db.listDocuments();
        docRefIt.forEach(docRef -> {
            docRef.delete();
        });
    }

    private String hashAndBase64(String data) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] b = digest.digest(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(b);
    }
}
