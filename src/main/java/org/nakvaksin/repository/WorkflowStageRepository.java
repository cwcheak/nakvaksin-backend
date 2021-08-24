package org.nakvaksin.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import org.nakvaksin.domain.WorkflowStage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@ApplicationScoped
public class WorkflowStageRepository {
    private final Logger log = LoggerFactory.getLogger(WorkflowStageRepository.class);

    @Inject
    Firestore firestore;

    public void createOrUpdateWorkflowStage(WorkflowStage stage) throws ExecutionException, InterruptedException {
        log.debug("createOrUpdateWorkflowStage : {}", stage);

        CollectionReference db = firestore.collection("workflow_stage");
        DocumentReference docRef = db.document(stage.getUserId());
        ApiFuture<DocumentSnapshot> future = docRef.get();
        DocumentSnapshot doc = future.get();
        if (doc.exists()) {
            log.debug("Workflow stage exists. Updating...");
            firestore.runTransaction(transaction -> {
                transaction.update(docRef, "currStage", stage.getCurrStage());
                transaction.update(docRef, "stageHist", stage.getStageHist());
                transaction.update(docRef, "lastModifiedDate", new Date());
                return null;
            });
        } else {
            log.debug("Workflow stage does not exists. Create new...");
            Map<String, Object> newUser = new HashMap<>();
            newUser.put("currStage", stage.getCurrStage());
            newUser.put("stageHist", stage.getStageHist());
            newUser.put("lastModifiedDate", new Date());
            docRef.set(newUser);
        }
    }

    public WorkflowStage getByUserId(String userId) throws ExecutionException, InterruptedException {
        log.debug("getByUserId : {}", userId);

        CollectionReference db = firestore.collection("workflow_stage");
        DocumentReference docRef = db.document(userId);
        ApiFuture<DocumentSnapshot> future = docRef.get();
        DocumentSnapshot doc = future.get();
        if (doc.exists()) {
            WorkflowStage stage = doc.toObject(WorkflowStage.class);
            log.debug("Workflow stage : {}", stage);
            return stage;
        }

        return null;
    }
}
