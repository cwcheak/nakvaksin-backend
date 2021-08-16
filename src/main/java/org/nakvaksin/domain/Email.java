package org.nakvaksin.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Email {
    private String from;
    private String to;
    private String subject;
    private String body;
}
