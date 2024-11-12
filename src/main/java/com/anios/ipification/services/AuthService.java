package com.anios.ipification.services;

import com.anios.ipification.feign.InfobipFeign;
import com.anios.ipification.requestDTO.Create2FARequestDTO;
import com.anios.ipification.requestDTO.CreateMessageTemplateRequestDTO;
import com.anios.ipification.requestDTO.PinRequestDTO;
import com.anios.ipification.requestDTO.VerifyPinRequestDTO;
import com.anios.ipification.responseDTO.Create2FAResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
public class AuthService {

    @Value("${infobip.api.key}")
    private String apiKey;

    @Autowired
    private InfobipFeign infobipFeign;


    public ResponseEntity<?> create2FAApplication(String appName) {
        Create2FARequestDTO create2FARequest = new Create2FARequestDTO();
        create2FARequest.setName(appName);
        create2FARequest.setEnabled(true);
        create2FARequest.setConfiguration(new Create2FARequestDTO.Configuration());

        String authorizationHeader = "App " + apiKey;
        return infobipFeign.create2FAApplication(create2FARequest, authorizationHeader);
    }


    public ResponseEntity<?> createMessageTemplate(String appId, CreateMessageTemplateRequestDTO messageTemplateRequest) {
        String authorizationHeader = "App " + apiKey;
        ResponseEntity<?> response = infobipFeign.createMessageTemplate(appId, messageTemplateRequest, authorizationHeader);
        return response;
    }

    public Map<String, Object> sendPin(String apiKey, boolean ncNeeded, PinRequestDTO pinRequestDTO) {
        String authHeader = "App " + apiKey;
        log.info("Auth Service apikey {}", authHeader);
        ResponseEntity<?> response = infobipFeign.sendPin(authHeader,/* ncNeeded, */pinRequestDTO);
        return (Map<String, Object>)response.getBody();
    }

    public ResponseEntity<?> verifyOtpPin(String apiKey, String pinId, VerifyPinRequestDTO verifyPinRequestDTO) {
       // VerifyPinRequestDTO verifyPinRequest = new VerifyPinRequest(pin);
        String authorizationHeader = "App " + apiKey;
        return infobipFeign.verifyPin(authorizationHeader, pinId, verifyPinRequestDTO);
    }
}
