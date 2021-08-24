package org.nakvaksin.service;

import org.nakvaksin.domain.WorkflowStage;
import org.nakvaksin.repository.WorkflowStageRepository;
import org.nakvaksin.service.exception.DBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class WorkflowStageService {
    private final Logger log = LoggerFactory.getLogger(WorkflowStageService.class);

    @Inject
    WorkflowStageRepository workflowStageRepository;

    public void createOrUpdateWorkflowStage(WorkflowStage stage) {
        log.debug("createOrUpdateWorkflowStage : {}", stage);

        try {
            workflowStageRepository.createOrUpdateWorkflowStage(stage);
        } catch (Exception e) {
            e.printStackTrace();
            throw new DBException("Error creating or updating workflow stage");
        }
    }

    public WorkflowStage getByUserId(String userId) {
        log.debug("getByUserId : {}", userId);

        try {
            return workflowStageRepository.getByUserId(userId);
        } catch (Exception e) {
            e.printStackTrace();
            throw new DBException("Error getting workflow stage by user id");
        }
    }
}
