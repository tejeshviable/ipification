package com.anios.ipification.services;

import com.anios.ipification.feign.InfobipFeign;
import com.anios.ipification.feign.WhatsappFeign;
import com.anios.ipification.requestDTO.MediaRequestDTO;
import com.anios.ipification.requestDTO.SmsRequestDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SmsService {

    @Autowired
    private InfobipFeign infobipFeign;

    @Autowired
    private WhatsappFeign whatsappFeign;


    public Object sendSms(String apiKey, SmsRequestDTO smsRequestDTO) {
        String authHeader = "App " + apiKey;

        log.info("Sending SMS Request: " + smsRequestDTO);

        log.info("Response from Infobip: " + infobipFeign.sendSms(authHeader, smsRequestDTO));
        return infobipFeign.sendSms(authHeader, smsRequestDTO);
    }

    public String sendWhatsAppMessage(MediaRequestDTO mediaRequestDTO) {
        try {
            log.info("media request DTO {}",mediaRequestDTO);
            return whatsappFeign.sendTextMessage(mediaRequestDTO).getBody();

        } catch (Exception e) {
            return "Failed to send message to " + mediaRequestDTO.getTo();
        }
    }
}
