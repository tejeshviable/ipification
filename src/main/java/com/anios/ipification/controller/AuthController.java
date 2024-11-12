package com.anios.ipification.controller;

import com.anios.ipification.requestDTO.CreateMessageTemplateRequestDTO;
import com.anios.ipification.requestDTO.PinRequestDTO;
import com.anios.ipification.requestDTO.VerifyPinRequestDTO;
import com.anios.ipification.responseDTO.Create2FAResponseDTO;
import com.anios.ipification.services.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@CrossOrigin()
@RequestMapping("/2fa")
public class AuthController {

    @Autowired
    AuthService authService;

    @PostMapping("/create")
    public ResponseEntity<?> create2FAApplication(@RequestParam String appName) {
        return authService.create2FAApplication(appName);
    }

    @PostMapping("/applications/{appId}/template")
    public ResponseEntity<?> createMessageTemplate(
            @PathVariable String appId,
            @RequestBody CreateMessageTemplateRequestDTO messageTemplateRequest) {
        return authService.createMessageTemplate(appId, messageTemplateRequest);
    }

    @PostMapping("/send-pin")
    public Map<String, Object> sendPin(
            @RequestHeader("Api-Key") String apiKey,
            @RequestParam(value = "ncNeeded", defaultValue = "true") boolean ncNeeded,
            @RequestBody PinRequestDTO pinRequestDTO
    ) {
        return (authService.sendPin(apiKey, ncNeeded, pinRequestDTO));
    }

    @PostMapping("/verify-otp/{pinId}")
    public ResponseEntity<?> verifyOtp(
            @RequestHeader("Api-Key") String apiKey,
            @PathVariable("pinId") String pinId,
            @RequestBody VerifyPinRequestDTO verifyPinRequestDTO
    ) {
        //VerifyPinResponse response = verificationService.verifyOtpPin(apiKey, pinId, pin);
        return new ResponseEntity<>(authService.verifyOtpPin(apiKey, pinId, verifyPinRequestDTO), HttpStatus.OK);
    }
}
