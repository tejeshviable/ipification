package com.anios.ipification.requestDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatusResponseDTO {

    private String txnId;
    private String otpTxnId;
    private String channel;
    private String message;
    private String errorMsg;
    private String status;
    private String errorCode;

}