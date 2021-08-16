package org.nakvaksin.web.rest.vm;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DataElement {
    private LanguageText text;
    private String value;
}
