package org.nakvaksin.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AdaSMSResponse {

    private Boolean success;
    private Message message;
    private String error;
    private String explain;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    class Message {
        private String price;
        private int recipients;
    }
}
