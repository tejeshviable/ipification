package com.anios.ipification.feign;

import com.anios.ipification.requestDTO.MediaRequestDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(value = "whatsapp-feign", url = "${feign.whatsapp.url}")
public interface WhatsappFeign {

    @PostMapping(value = "/text", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> sendTextMessage(@RequestBody MediaRequestDTO mediaRequestDTO) throws JsonProcessingException;

}
