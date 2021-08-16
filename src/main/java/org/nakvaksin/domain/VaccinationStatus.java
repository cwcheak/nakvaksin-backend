package org.nakvaksin.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VaccinationStatus {
    private String userId;

    private String vacPreviousJson;

    private Date vacPreviousDate;

    private String vacCurrentJson;

    private Date vacCurrentDate;
}
