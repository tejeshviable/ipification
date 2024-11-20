package com.anios.ipification.controller;

import com.anios.ipification.requestDTO.GenerateUrlRequestDTO;
import com.anios.ipification.requestDTO.MobileRequestDTO;
import com.anios.ipification.requestDTO.RedisDto;
import com.anios.ipification.services.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping()
@Slf4j
public class UserController {

    @Autowired
    UserService userService;

    @GetMapping("/callback")
    @ResponseBody
    public ResponseEntity<?> handleRedirect(
            @RequestParam(value = "code", required = false) String code,
            @RequestParam(value = "state", required = false) String state,
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "error_description", required = false) String errorDescription) throws JsonProcessingException {

        log.info("Received code: {}", code);
        log.info("Received state: {}", state);
        log.info("Received error: {}", error);
        log.info("Received errorDescription: {}", errorDescription);
        return new ResponseEntity<>(userService.saveVerificationStatus(code,state, error, errorDescription), HttpStatus.OK);
    }

    @GetMapping(value = "/status/{txnId}")
    public ResponseEntity<RedisDto> getUserStatus(@PathVariable("txnId") String txnId){
        return new ResponseEntity<>(userService.getUserStatus(txnId),HttpStatus.OK);
    }

    @PostMapping("/authenticate")
    public ResponseEntity<?> authenticateUser(@RequestBody GenerateUrlRequestDTO generateUrlRequestDTO) throws JsonProcessingException {
        return ResponseEntity.ok(userService.authenticateUser(generateUrlRequestDTO));
    }

}