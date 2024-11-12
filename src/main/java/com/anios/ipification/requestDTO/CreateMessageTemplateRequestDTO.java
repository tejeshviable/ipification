package com.anios.ipification.requestDTO;

import lombok.Data;

@Data
public class CreateMessageTemplateRequestDTO {

    private String language = "en";
    private String messageText;
    private int pinLength;
    private String pinType;
    private Regional regional;
   // private String repeatDTMF;
    private String senderId;
    private Double speechRate;
    private String voiceName;

    @Data
    public static class Regional {
        private IndiaDLT indiaDlt;
    }

    @Data
    public static class IndiaDLT {
        private String contentTemplateId;
        private String principalEntityId;
    }
}
