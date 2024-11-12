package com.anios.ipification.requestDTO;

import com.anios.ipification.enums.WhatsappMediaType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class MediaRequestDTO {
    @JsonProperty("messaging_product")
    private String messagingProduct;
    private String to;
    private WhatsappMediaType type;
    private WhatsappTemplate template;

    @Data
    public static class WhatsappTemplate {
        private String name;
        private Language language;
        @Data
        public static class Language{
            public String code;
        }
    }

}
