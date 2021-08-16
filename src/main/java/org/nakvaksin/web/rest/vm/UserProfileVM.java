package org.nakvaksin.web.rest.vm;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class UserProfileVM {
    private UserElement user;
    private List<StageElement> vacStatus;
    private SubscriptionElement subscription;

    public UserProfileVM() {
        user = new UserElement();
        vacStatus = new ArrayList<StageElement>();
        subscription = new SubscriptionElement();
    }

    public void addStages(StageElement[] stageElements) {
        if(stageElements == null) return;
        for(StageElement s: stageElements) {
            this.vacStatus.add(s);
        }
    }
}
