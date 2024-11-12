package com.anios.ipification.controller;

import com.anios.ipification.requestDTO.MediaRequestDTO;
import com.anios.ipification.requestDTO.SmsRequestDTO;
import com.anios.ipification.services.SmsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@RequestMapping("/sms")
public class SmsController {

    @Autowired
    private SmsService smsService;

    @PostMapping("/send")
    public ResponseEntity<?> sendSms(
            @RequestHeader("Api-Key") String apiKey,
            @RequestBody SmsRequestDTO smsRequestDTO)
    {
        return new ResponseEntity<>(smsService.sendSms(apiKey,smsRequestDTO), HttpStatus.OK);
    }

    @PostMapping("/send-whatsapp-message")
    public String sendWhatsAppMessage(
            @RequestBody MediaRequestDTO mediaRequestDTO) {

        return smsService.sendWhatsAppMessage(mediaRequestDTO);
    }




}
