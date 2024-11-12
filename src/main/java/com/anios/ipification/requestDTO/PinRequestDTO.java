package com.anios.ipification.requestDTO;

import lombok.Data;

import java.util.Map;

@Data
public class PinRequestDTO {

    private String applicationId;
    private String messageId;
    private String from;
    private String to;
    private Map<String, String> placeholders;
    private boolean ncNeeded = true;
}