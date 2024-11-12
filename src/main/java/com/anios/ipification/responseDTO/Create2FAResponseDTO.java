package com.anios.ipification.responseDTO;

import lombok.Data;

@Data
public class Create2FAResponseDTO {
    private String id;
    private String name;
    private boolean enabled;
}
