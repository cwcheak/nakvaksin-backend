package org.nakvaksin.web.rest.vm;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class StageElement {
    private String timestamp;
    private LanguageText headerText;
    private String state;
    private List<DataElement> data;
    private List<DataElement> action;
}
