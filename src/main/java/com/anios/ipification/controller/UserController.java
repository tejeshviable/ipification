package com.anios.ipification.controller;

import com.anios.ipification.requestDTO.GenerateUrlRequestDTO;
import com.anios.ipification.requestDTO.MobileRequestDTO;
import com.anios.ipification.requestDTO.RedisDto;
import com.anios.ipification.services.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping()
public class UserController {

    @Autowired
    UserService userService;

    @GetMapping("/callback")
    @ResponseBody
    public ResponseEntity<?> handleRedirect(
            @RequestParam("code") String code,
            @RequestParam("state") String state,
            @RequestParam(value = "error", required = false) String error) throws JsonProcessingException {

        // Log the received parameters
        System.out.println("Received code: " + code);
        System.out.println("Received state: " + state);
        return new ResponseEntity<>(userService.saveVerificationStatus(code,state), HttpStatus.OK);
    }

    @GetMapping(value = "/status")
    public ResponseEntity<RedisDto> getUserStatus(@PathVariable MobileRequestDTO mobileRequestDTO){
        return new ResponseEntity<>(userService.getUserStatus(mobileRequestDTO.getMobileNumber()),HttpStatus.OK);
    }

    @PostMapping("/authenticate")
    public ResponseEntity<?> authenticateUser(@RequestBody GenerateUrlRequestDTO generateUrlRequestDTO) throws JsonProcessingException {
        return new ResponseEntity<>(userService.authenticateUser(generateUrlRequestDTO), HttpStatus.MOVED_TEMPORARILY);
    }

}