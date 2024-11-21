package com.anios.ipification.responseDTO;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StatusResponseDTO {

    private String txnId;
    private String otpTxnId;
    private String channel;
    private String message;
    private String errorMsg;
    private String status;
    private String errorCode;

}
