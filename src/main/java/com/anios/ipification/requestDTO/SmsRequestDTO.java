package com.anios.ipification.requestDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SmsRequestDTO {

    private List<Message> messages;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Message{
        private String from;
        private List<Destination> destinations;
        private String text;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Destination {
        private String to;
    }

}
