package org.nakvaksin.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonRootName(value = "employeeInfo")
public class User {

    @JsonProperty("employeeId")
    private String userId;

    @JsonProperty("userName")
    private String username;

    @JsonProperty("displayName")
    private String displayName;

    @JsonProperty("phoneNumber")
    private String phoneNumber;

    private String email;

    private String token;

    private Date lastModifiedDate;
}
