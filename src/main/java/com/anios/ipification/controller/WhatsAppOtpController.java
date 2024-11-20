package com.anios.ipification.controller;

import com.anios.ipification.requestDTO.OtpRecordRequestDTO;
import com.anios.ipification.responseDTO.OtpResponseDTO;
import com.anios.ipification.services.WhatsAppOtpService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/otp")
public class WhatsAppOtpController {

    @Autowired
    WhatsAppOtpService whatsAppOtpService;

    @PostMapping("/generate-whatsapp-otp")
    public ResponseEntity<?> generateWhatsappOtp(@RequestParam String mobileNo) throws JsonProcessingException {
        return whatsAppOtpService.generateWhatsappOtp(mobileNo);
    }

    @PostMapping("/verify-whatsapp-otp")
    public ResponseEntity<OtpResponseDTO> verifyWhatsappOtp(@RequestBody OtpRecordRequestDTO otpRecordRequestDTO) {
       OtpResponseDTO otpResponse =  whatsAppOtpService.verifyWhatsappOtp(otpRecordRequestDTO);
       return ResponseEntity.ok(otpResponse);
    }


}
