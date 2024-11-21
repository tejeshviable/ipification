package com.anios.ipification.requestDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PinRequestDTO {

    private String applicationId;
    private String messageId;
    private String from;
    private String to;
    private Map<String, String> placeholders;
    private boolean ncNeeded = true;
}