package com.anios.ipification.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GenerateUrlResponseDTO {
    private String requestId;
    private String redirectionUrl;
}
