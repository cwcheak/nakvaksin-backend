package org.nakvaksin.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TokenHistory {

    private String token;

    private String userId;

    private Date createdDate;
}
