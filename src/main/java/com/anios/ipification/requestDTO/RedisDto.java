package com.anios.ipification.requestDTO;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class RedisDto implements Serializable {
    private String requestId;
    private String status;
    private String errorMsg;
}
