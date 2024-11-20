package com.anios.ipification.requestDTO;

import com.anios.ipification.enums.WhatsappMediaType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MediaRequestDTO {
    @JsonProperty("messaging_product")
    private String messagingProduct;
    private String to;
    private WhatsappMediaType type;
    private WhatsappTemplate template;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class WhatsappTemplate {
        private String name;
        private Language language;
        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public static class Language{
            public String code;
        }
    }

}
