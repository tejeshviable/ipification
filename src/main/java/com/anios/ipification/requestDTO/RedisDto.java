package com.anios.ipification.requestDTO;

import lombok.Data;

import java.io.Serializable;

@Data
public class RedisDto implements Serializable {
    private String requestId;
    private String status;
}
