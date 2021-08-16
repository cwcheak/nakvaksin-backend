package org.nakvaksin.domain;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class Notification {
    private String id;
    private String userId;
    private List<NotificationChannelStatus> channels;
    private boolean allSent;
    private String currVacStatusJson;
    private String prevVacStatusJson;
    private Date createdDate;
}
