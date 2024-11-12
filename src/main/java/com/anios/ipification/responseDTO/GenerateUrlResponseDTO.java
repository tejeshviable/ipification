package com.anios.ipification.responseDTO;

import lombok.Data;

@Data
public class GenerateUrlResponseDTO {
    private String requestId;
    private String redirectionUrl;
}
