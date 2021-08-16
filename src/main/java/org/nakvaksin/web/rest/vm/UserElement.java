package org.nakvaksin.web.rest.vm;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserElement {
    private String userId;

    private String username;

    private String displayName;

    private String phoneNumber;

    private String email;

    @JsonIgnore
    private String token;
}
