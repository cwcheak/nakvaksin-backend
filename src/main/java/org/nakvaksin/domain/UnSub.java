package org.nakvaksin.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UnSub {
    private String key;
    private String contact;
    private boolean unsub;
}
