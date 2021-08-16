package org.nakvaksin.web.rest.vm;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SubscriptionElement {

    private String userPhoneNumber;

    private String userEmail;

    private String familyPhoneNumber;

    private String familyEmail;
}
