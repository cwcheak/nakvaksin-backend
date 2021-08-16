package org.nakvaksin.web.rest.vm;

import lombok.Data;

@Data
public class SubscriptionVM {

    private String userPhoneNumber;

    private String userEmail;

    private String familyPhoneNumber;

    private String familyEmail;
}
