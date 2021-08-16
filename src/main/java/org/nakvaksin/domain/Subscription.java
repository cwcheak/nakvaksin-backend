package org.nakvaksin.domain;

import lombok.Data;

import java.util.Date;

@Data
public class Subscription {

    private String userId;

    private String userPhoneNumber;

    private String userEmail;

    private String familyPhoneNumber;

    private String familyEmail;

    private Date createdDate;

    private Date lastModifiedDate;
}
