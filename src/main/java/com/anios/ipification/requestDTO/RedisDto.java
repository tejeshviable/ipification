package com.anios.ipification.requestDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RedisDto implements Serializable {
    //private static final long serialVersionUID = 753684517527574629L;
    private String mobileNumber;
    private String status;
    private String channel;
    private String message;
    private String errorMessage;
}
