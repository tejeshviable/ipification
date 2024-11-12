package com.anios.ipification.requestDTO;

import lombok.Data;
import java.util.List;

@Data
public class SmsRequestDTO {

    private List<Message> messages;

    @Data
    public static class Message{
        private String from;
        private List<Destination> destinations;
        private String text;
    }

    @Data
    public static class Destination {
        private String to;
    }

}
