package org.nakvaksin.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
public class WorkflowStage {

    public static final int STAGE_INIT = 10;
    public static final int STAGE_FIRST_DOSE_APPOINTMENT_SCHEDULED = 20;
    public static final int STAGE_FIRST_DOSE_APPOINTMENT_CONFIRMATION_REMINDER_1 = 30;
    public static final int STAGE_FIRST_DOSE_APPOINTMENT_CONFIRMATION_REMINDER_2 = 40;
    public static final int STAGE_FIRST_DOSE_APPOINTMENT_CONFIRMATION_REMINDER_3 = 50;
    public static final int STAGE_FIRST_DOSE_APPOINTMENT_PRIOR_DAY_REMINDER = 50;
    public static final int STAGE_FIRST_DOSE_COMPLETED = 60;
    public static final int STAGE_SECOND_DOSE_APPOINTMENT_SCHEDULED = 70;
    public static final int STAGE_SECOND_DOSE_APPOINTMENT_CONFIRMATION_REMINDER_1 = 80;
    public static final int STAGE_SECOND_DOSE_APPOINTMENT_CONFIRMATION_REMINDER_2 = 90;
    public static final int STAGE_SECOND_DOSE_APPOINTMENT_CONFIRMATION_REMINDER_3 = 100;
    public static final int STAGE_SECOND_DOSE_APPOINTMENT_PRIOR_DAY_REMINDER = 110;
    public static final int STAGE_SECOND_DOSE_COMPLETED = 120;

    private String userId;

    private int currStage;

    private String stageHist;

    private Date lastModifiedDate;
}
