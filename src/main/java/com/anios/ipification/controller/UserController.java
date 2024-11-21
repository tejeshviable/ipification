package com.anios.ipification.controller;

import com.anios.ipification.requestDTO.GenerateUrlRequestDTO;
import com.anios.ipification.requestDTO.RedisDto;
import com.anios.ipification.services.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Enumeration;

@RestController
@RequestMapping()
@Slf4j
public class UserController {

    @Autowired
    UserService userService;

    @GetMapping("/callback")
    @ResponseBody
    public ResponseEntity<?> handleRedirect(
//            @RequestParam(value = "client_id", required = false) String clientId,
            @RequestParam(value = "code", required = false) String code,
            @RequestParam(value = "state", required = false) String state,
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "error_description", required = false) String errorDescription,
//            @RequestHeader(value = "client_id", required = false) String clientIdHeader,
            HttpServletRequest request) throws JsonProcessingException {

        String referer = request.getHeader("Referer");
        System.out.println("Referer: " + referer);
        String clientId = null;
        if (referer != null && referer.contains("client_id")) {
            clientId = referer.split("client_id=")[1].split("&")[0];
        }

        log.info("Received clientId: {}", clientId);
        log.info("Received code: {}", code);
        log.info("Received state: {}", state);
        log.info("Received error: {}", error);
        log.info("Received errorDescription: {}", errorDescription);
        return new ResponseEntity<>(userService.verificationOnCallback(clientId, code, state, error, errorDescription), HttpStatus.OK);
    }

    @GetMapping(value = "/status/{txnId}")
    public ResponseEntity<RedisDto> getUserStatus(@PathVariable("txnId") String txnId) {
        return new ResponseEntity<>(userService.getUserStatus(txnId), HttpStatus.OK);
    }

    @PostMapping("/authenticate")
    public ResponseEntity<?> authenticateUser(@RequestParam(value = "clientId") String clientId,
                                              @RequestBody GenerateUrlRequestDTO generateUrlRequestDTO) throws JsonProcessingException {
        return ResponseEntity.ok(userService.authenticateUser(clientId, generateUrlRequestDTO));
    }

}