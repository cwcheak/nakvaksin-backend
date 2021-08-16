package org.nakvaksin.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationChannelStatus {

    public static final String CHANNEL_TYPE_SMS = "SMS";
    public static final String CHANNEL_TYPE_EMAIL = "EMAIL";

    private String type;
    private String text;
    private String contact;
    private Date sentDate;
}
