package com.anios.ipification.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Create2FAResponseDTO {
    private String id;
    private String name;
    private boolean enabled;
}
