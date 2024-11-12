package com.anios.ipification.feign;

import com.anios.ipification.requestDTO.*;
import com.anios.ipification.responseDTO.Create2FAResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@FeignClient(value = "InfobipClient", url = "${feign.infobip.url}")
public interface InfobipFeign {

    @PostMapping(value = "/sms/2/text/advanced")
    ResponseEntity<?> sendSms(
            @RequestHeader("Authorization") String authorization,
            @RequestBody SmsRequestDTO smsRequest
    );

    @PostMapping("/2fa/2/applications")
    ResponseEntity<?> create2FAApplication(
            @RequestBody Create2FARequestDTO create2FARequest,
            @RequestHeader("Authorization") String apiKey
    );

    @PostMapping("/2fa/2/applications/{appId}/messages")
    ResponseEntity<?> createMessageTemplate(
            @PathVariable("appId") String appId,
            @RequestBody CreateMessageTemplateRequestDTO messageTemplateRequest,
            @RequestHeader("Authorization") String apiKey
    );

    @PostMapping(value = "/2fa/2/pin")
    ResponseEntity<?> sendPin(
            @RequestHeader("Authorization") String apiKey,
            //@RequestParam(value = "ncNeeded", defaultValue = "true") boolean ncNeeded,
            @RequestBody PinRequestDTO pinRequest
    );

    @PostMapping("/2fa/2/pin/{pinId}/verify")
    ResponseEntity<?> verifyPin(
            @RequestHeader("Api-Key") String apiKey,
            @PathVariable("pinId") String pinId,
            @RequestBody VerifyPinRequestDTO verifyPinRequest
    );

}
